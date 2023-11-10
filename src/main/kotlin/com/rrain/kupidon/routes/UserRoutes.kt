package com.rrain.kupidon.routes

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon.entity.app.Sex
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.routes.util.RequestError
import com.rrain.kupidon.service.DatabaseService.userServ
import com.rrain.kupidon.service.EmailService
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.db.table.UserTbirthDate
import com.rrain.kupidon.service.db.table.UserTemailVerified
import com.rrain.kupidon.service.db.table.UserTname
import com.rrain.kupidon.util.extension.respondInvalidInputBody
import com.rrain.kupidon.util.extension.respondNoUser
import com.rrain.kupidon.util.extension.use
import com.rrain.kupidon.util.toLocalDate
import com.rrain.kupidon.util.toZonedDateTime
import com.rrain.kupidon.util.zonedDateTimePattern
import com.rrain.kupidon.util.zonedNow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.r2dbc.postgresql.api.PostgresqlException
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.R2dbcBadGrammarException
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.commons.mail.EmailException
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


object UserRoutes {
  const val base = "/api/user"
  const val current = "$base/current"
  const val create = "$base/create"
  const val update = "$base/update"
  const val verifyEmail = "$base/verify/email"
  const val getById = "$base/getById/{id}"
  
  const val verifyTokenParamName = "verificationToken"
}

fun Application.configureUserRoutes(){
  
  routing {
    
    
    
    authenticate {
      get(UserRoutes.current) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        val user = userServ.getById(userId)
        user ?: return@get call.respondNoUser()
        call.respond(object {
          val user = user.copy(pwd=null)
        })
      }
    }
    
    
    
    get(UserRoutes.getById) {
      var user = try { userServ.getById(call.parameters["id"]!!) }
      catch (ex: R2dbcBadGrammarException){
        null
      }
      
      user = user?.copy(pwd=null)
      call.respond(object {
        val user = user
      })
    }
    
    
    
    data class UserCreateReq(
      val email: String,
      val pwd: String,
      val name: String,
      val sex: Sex,
      val birthDate: ZonedDateTime
    )
    post(UserRoutes.create) {
      val userToCreate = try {
        call.receive<UserCreateReq>()
      } catch (ex: Exception){
        return@post call.respondInvalidInputBody()
      }
      
      if (!userToCreate.email.matches(Regex("^.+@.+$"))){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = RequestError.INVALID_INPUT_BODY.name
          val msg = "Invalid email format"
        })
      }
      if (userToCreate.pwd.length<6){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = RequestError.INVALID_INPUT_BODY.name
          val msg = "Password must be at least 6 chars length"
        })
      }
      if (userToCreate.name.isEmpty()){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = RequestError.INVALID_INPUT_BODY.name
          val msg = "Name must not be empty"
        })
      }
      val nowWithUserZone = zonedNow()
        .withZoneSameInstant(userToCreate.birthDate.zone)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
      if (ChronoUnit.YEARS.between(userToCreate.birthDate, nowWithUserZone)<18){
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = RequestError.INVALID_INPUT_BODY.name
          val msg = "You must be at least 18 years old"
        })
      }
      
      
      val tryUser = User(
        email = userToCreate.email,
        pwd = userToCreate.pwd,
        name = userToCreate.name,
        sex = userToCreate.sex,
        birthDate = userToCreate.birthDate.toLocalDate(),
      )
      
      var user = try { userServ.create(tryUser) }
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
      
      val id = user.id!!
      val verificationToken = JwtService.generateVerificationAccessToken(id, user.email!!)
      
      launch {
        try {
          EmailService.sendVerificationEmail(
            user,
            call.request.origin.run {
              "$scheme://$serverHost:$serverPort${UserRoutes.verifyEmail}?${UserRoutes.verifyTokenParamName}=$verificationToken"
            }
          )
        } catch (ex: EmailException) {
          // Ошибка отправки
          // todo validate if email was sent successfully
          ex.printStackTrace()
        }
      }
      
      val roles = user.roles
      
      val domain = call.request.origin.serverHost
      
      val accessToken = JwtService.generateAccessToken(id, roles)
      val refreshToken = JwtService.generateRefreshToken(id)
      
      // сделать позже save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(refreshToken,domain)
      )
      user = user.copy(pwd=null)
      call.respond(object {
        val accessToken = accessToken
        val user = user
      })
    }
    
    
    
    
    
    
    
    
    authenticate {
      put(UserRoutes.update) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        
        val dataAsMap = try {
          call.receive<MutableMap<String, Any?>>()
        } catch (ex: Exception) {
          return@put call.respondInvalidInputBody()
        }
        
        
        val unknownProperties = dataAsMap.keys - setOf("name","birthDate")
        if (unknownProperties.isNotEmpty()){
          return@put call.respond(HttpStatusCode.BadRequest, object {
            val code = RequestError.INVALID_INPUT_BODY.name
            val msg = "Unknown properties: $unknownProperties"
          })
        }
        
        
        if ("name" in dataAsMap) {
          val value = dataAsMap["name"]
          if (value !is String)
            return@put call.respond(HttpStatusCode.BadRequest, object {
              val code = RequestError.INVALID_INPUT_BODY.name
              val msg = "Invalid 'name' type"
            })
          if (value.isEmpty())
            return@put call.respond(HttpStatusCode.BadRequest, object {
              val code = RequestError.INVALID_INPUT_BODY.name
              val msg = "Name must not be empty"
            })
        }
        
        
        if ("birthDate" in dataAsMap){
          val value = dataAsMap["birthDate"]
          try {
            if (value !is String) throw RuntimeException()
            val birthDate = value.toZonedDateTime()
            val nowWithUserZone = zonedNow()
              .withZoneSameInstant(birthDate.zone)
              .withHour(0)
              .withMinute(0)
              .withSecond(0)
              .withNano(0)
            if (ChronoUnit.YEARS.between(birthDate, nowWithUserZone)<18){
              return@put call.respond(HttpStatusCode.BadRequest, object {
                val code = RequestError.INVALID_INPUT_BODY.name
                val msg = "You must be at least 18 years old"
              })
            }
            dataAsMap["birthDate"] = value.toLocalDate()
          } catch (ex: Exception){
            return@put call.respond(HttpStatusCode.BadRequest, object {
              val code = RequestError.INVALID_INPUT_BODY.name
              val msg =
                """'birthDate' must be string "$zonedDateTimePattern", for example "2005-11-10T00:00:00.000+08:00""""
            })
          }
          
        }
        
        
        
        
        
        val conn = userServ.pool.create().awaitSingle()
        conn.transactionIsolationLevel = IsolationLevel.SERIALIZABLE
        var user = conn.use {
          val user = userServ.getById(userId)
          
          user ?: return@put call.respondNoUser()
          
          userServ.update(
            userId,
            dataAsMap.mapKeys { (k,_) -> when(k){
              "name" -> UserTname
              "birthDate" -> UserTbirthDate
              else -> TODO("Implement update of other columns")
            } }
          )
        }
        
        
        user = user.copy(pwd=null)
        call.respond(object {
          val user = user
        })
      }
    }
    
    
    
    
    // todo Language
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
      // Modified Token - Токен модифицирован (подделан)
      catch (ex: SignatureVerificationException){
        return@get call.respondText(
          status = HttpStatusCode.BadRequest,
          text = getHtmlResponse("Ошибка! Токен верификации модифицирован (подделан)"),
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
        // пользователь не найден
        if (user==null){
          return@get call.respondText(
            status = HttpStatusCode.BadRequest,
            text = getHtmlResponse("Ошибка! Пользователь с таким id не найден"),
            contentType = ContentType.Text.Html
          )
        }
        // пользователь найден, но у него другой имэйл
        if (user.email != decodedJwt.claims["email"]!!.asString()){
          return@get call.respondText(
            status = HttpStatusCode.BadRequest,
            text = getHtmlResponse("Ошибка! Данный токен верификации предназначен для другого email"),
            contentType = ContentType.Text.Html
          )
        }
        // пользователь найден, но имэйл уже верифицирован
        if (user.emailVerified!!){
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