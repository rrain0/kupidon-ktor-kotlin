package com.rrain.kupidon.routes

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon.entity.app.Sex
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.service.DatabaseService.roleServ
import com.rrain.kupidon.service.DatabaseService.userServ
import com.rrain.kupidon.service.EmailService
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.db.table.UserTemailVerified
import com.rrain.kupidon.service.db.table.UserTname
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import io.r2dbc.postgresql.api.PostgresqlException
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.R2dbcBadGrammarException
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.commons.mail.EmailException
import org.intellij.lang.annotations.Language
import java.time.LocalDate



object UserRoutes {
  val base = "/api/user"
  val current = "$base/current"
  val create = "$base/create"
  val update = "$base/update"
  val verifyEmail = "$base/verify/email"
  val getById = "$base/getById/{id}"
  
  val verifyTokenParamName = "verification-token"
}

fun Application.configureUserRoutes(){
  
  routing {
    
    
    
    authenticate {
      get(UserRoutes.current) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        val user = userServ.getById(userId)
        user ?: return@get call.respond(HttpStatusCode.BadRequest, object {
          val code = "NO_USER"
          val msg = "No user with such id"
        })
        call.respond(object {
          val user = user.copy(pwd=null)
        })
      }
    }
    
    
    
    get(UserRoutes.getById) {
      val user = try { userServ.getById(call.parameters["id"]!!) }
      catch (ex: R2dbcBadGrammarException){
        null
      }
      
      call.respond(object {
        val user = user?.copy(pwd=null)
      })
    }
    
    
    
    data class UserCreateReq(
      val email: String,
      val pwd: String,
      val name: String,
      val sex: Sex,
      val birthDate: LocalDate
    )
    post(UserRoutes.create) {
      val userCreate = try {
        call.receive<UserCreateReq>()
      } catch (ex: Exception){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = "INVALID_INPUT_BODY"
          val msg = "Invalid request body format"
        })
      }
      
      if (!userCreate.email.matches(Regex("^.+@.+$"))){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = "INVALID_INPUT_BODY__INVALID_EMAIL_FORMAT"
          val msg = "Invalid email format"
        })
      }
      if (userCreate.pwd.length<6){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = "INVALID_INPUT_BODY__INVALID_PWD_FORMAT"
          val msg = "Password must be at least 6 chars length"
        })
      }
      if (userCreate.name.isEmpty()){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = "INVALID_INPUT_BODY__INVALID_NAME_FORMAT"
          val msg = "Name must not be empty"
        })
      }
      
      val tryUser = User(
        email = userCreate.email,
        pwd = userCreate.pwd,
        name = userCreate.name,
        sex = userCreate.sex,
        birthDate = userCreate.birthDate,
      )
      
      val user = try { userServ.create(tryUser) }
      catch (ex: R2dbcDataIntegrityViolationException){
        if (ex is PostgresqlException){
          ex.errorDetails.constraintName.let { cons ->
            if (cons.isPresent && cons.get() == "User_email_key"){
              return@post call.respond(HttpStatusCode.BadRequest, object {
                val code = "DUPLICATE_EMAIL"
                val msg = "User with such email already exists"
              })
            }
          }
        }
        throw ex
      }
      
      val verificationToken = JwtService.generateVerificationAccessToken(user.id!!, user.email!!)
      
      launch {
        try {
          EmailService.sendVerificationEmail(
            user,
            call.request.origin.run {
              "$scheme://$serverHost:$serverPort${UserRoutes.verifyEmail}?${UserRoutes.verifyTokenParamName}=$verificationToken"
            }
          )
        } catch (ex: EmailException) { // Ошибка отправки
          // сделать позже validate if email was sent successfully
          ex.printStackTrace()
        }
      }
      
      val id = user.id!!
      val roles = user.roles
      
      val domain = call.request.origin.serverHost
      
      val accessToken = JwtService.generateAccessToken(id, roles)
      val refreshToken = JwtService.generateRefreshToken(id)
      
      // сделать позже save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(refreshToken,domain)
      )
      call.respond(object {
        val accessToken = accessToken
        val user = user.copy(pwd=null)
      })
    }
    
    
    
    
    
    
    
    data class UserUpdateReq(
      val map: MutableMap<String,Any?>
    ){
      val name: String by map
    }
    authenticate {
      put(UserRoutes.update) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        
        val dataAsMap = try {
          call.receive<MutableMap<String, Any?>>()
        } catch (ex: Exception) {
          return@put call.respond(HttpStatusCode.BadRequest, object {
            val code = "INVALID_INPUT_BODY"
            val msg = "Invalid request body format"
          })
        }
        
        if ("name" in dataAsMap && dataAsMap["name"] !is String) {
          return@put call.respond(HttpStatusCode.BadRequest, object {
            val code = "INVALID_INPUT_BODY"
            val msg = "Invalid 'name' type"
          })
        }
        
        
        val updateUser = UserUpdateReq(dataAsMap)
        
        if ("name" in dataAsMap && updateUser.name.isEmpty()) {
          return@put call.respond(HttpStatusCode.BadRequest, object {
            val code = "INVALID_INPUT_BODY__INVALID_NAME_FORMAT"
            val msg = "Name must not be empty"
          })
        }
        
        
        
        
        
        /*val conn = userServ.pool.create().awaitSingle()
        try {
          conn.transactionIsolationLevel = IsolationLevel.SERIALIZABLE
          conn.beginTransaction()
          val user = userServ.getById(decodedJwt.subject, conn)
          if (user==null){ // пользователь не найден
            return@get call.respondText(
              status = HttpStatusCode.BadRequest,
              text = getHtmlResponse("Ошибка! Пользователь с таким id не найден"),
              contentType = ContentType.Text.Html
            )
          }
          if (user.email != decodedJwt.claims["email"]!!.asString()){ // пользователь найден, но у него другой имэйл
            return@get call.respondText(
              status = HttpStatusCode.BadRequest,
              text = getHtmlResponse("Ошибка! Данный токен верификации предназначен для другого email"),
              contentType = ContentType.Text.Html
            )
          }
          if (user.emailVerified!!){ // пользователь найден, но имэйл уже верифицирован
            return@get call.respondText(
              status = HttpStatusCode.OK,
              text = getHtmlResponse("Успешно завершено! Ваш email уже был подтверждён ранее"),
              contentType = ContentType.Text.Html
            )
          }
          userServ.update(
            decodedJwt.subject,
            mapOf(UserTemailVerified to true)
          )
          conn.commitTransaction()
        } finally {
          conn.close()
        }*/
        
        
        
        
        
        val user = userServ.update(
          userId,
          dataAsMap.mapKeys { (k,_) -> when(k){
            "name" -> UserTname
            else -> TODO("Implement update of other columns")
          } }
        )
        
        
        // todo check id in transaction
        user ?: return@put call.respond(HttpStatusCode.BadRequest, object {
          val code = "NO_USER"
          val msg = "There is no user with such id"
        })
        
        
        
        
        call.respond(object {
          val user = user.copy(pwd=null)
        })
      }
    }
    
    
    
    
    
    get(UserRoutes.verifyEmail) {
      val verificationToken = call.request.queryParameters[UserRoutes.verifyTokenParamName]
      
      fun getHtmlResponse(msg: String): String {
        @Language("html") val html = """
          <html lang="ru">
          <head>
            <meta charset="utf-8">
            <title>Верификация почты в приложении Купидон</title>
          </head>
          <body>
            <p style="font-size: 2em">${msg}</p>
          </body>
          </html>
        """.trimIndent()
        return html
      }
      
      val verifier = JwtService.emailValidationAccessTokenVerifier
      val decodedJwt = try { verifier.verify(verificationToken) }
      // Token was encoded by wrong algorithm. Required HMAC256.
      catch (ex: AlgorithmMismatchException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен верификации закодирован неперавильным алгоритмом"),
          contentType = ContentType.Text.Html
        )
      }
      // Damaged Token - Токен повреждён и не может быть декодирован
      catch (ex: JWTDecodeException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен верификации повреждён"),
          contentType = ContentType.Text.Html
        )
      }
      // Modified Token - Токен умышленно модифицирован (подделан)
      catch (ex: SignatureVerificationException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен верификации умышленно модифицирован (подделан)"),
          contentType = ContentType.Text.Html
        )
      }
      // Token has expired
      catch (ex: TokenExpiredException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен верификации устарел"),
          contentType = ContentType.Text.Html
        )
      }
      // Token lacks some required claims
      catch (ex: MissingClaimException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен не является токеном верификации"),
          contentType = ContentType.Text.Html
        )
      }
      // Token claim value is incorrect
      catch (ex: IncorrectClaimException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен не является токеном верификации"),
          contentType = ContentType.Text.Html
        )
      }
      // Common Verification Exception
      catch (ex: JWTVerificationException) {
        ex.printStackTrace()
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Ошибка проверки токена верификации"),
          contentType = ContentType.Text.Html
        )
      }
      
      
      val conn = userServ.pool.create().awaitSingle()
      try {
        conn.transactionIsolationLevel = IsolationLevel.SERIALIZABLE
        conn.beginTransaction()
        val user = userServ.getById(decodedJwt.subject, conn)
        if (user==null){ // пользователь не найден
          return@get call.respondText(
            status = HttpStatusCode.BadRequest,
            text = getHtmlResponse("Ошибка! Пользователь с таким id не найден"),
            contentType = ContentType.Text.Html
          )
        }
        if (user.email != decodedJwt.claims["email"]!!.asString()){ // пользователь найден, но у него другой имэйл
          return@get call.respondText(
            status = HttpStatusCode.BadRequest,
            text = getHtmlResponse("Ошибка! Данный токен верификации предназначен для другого email"),
            contentType = ContentType.Text.Html
          )
        }
        if (user.emailVerified!!){ // пользователь найден, но имэйл уже верифицирован
          return@get call.respondText(
            status = HttpStatusCode.OK,
            text = getHtmlResponse("Успешно завершено! Ваш email уже был подтверждён ранее"),
            contentType = ContentType.Text.Html
          )
        }
        userServ.update(
          decodedJwt.subject,
          mapOf(UserTemailVerified to true)
        )
        conn.commitTransaction()
      } finally {
        conn.close()
      }
      
      return@get call.respondText(
        status = HttpStatusCode.OK,
        text = getHtmlResponse("Успешно завершено! Ваша почта успешно верифицирована!"),
        contentType = ContentType.Text.Html
      )
    }
    
    
  }
  
}