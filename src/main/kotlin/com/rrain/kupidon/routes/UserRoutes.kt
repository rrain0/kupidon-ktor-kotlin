package com.rrain.kupidon.routes

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.routes.util.RequestError
import com.rrain.kupidon.service.DatabaseService.userServ
import com.rrain.kupidon.service.EmailService
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.service.db.table.*
import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.util.extension.respondBadRequest
import com.rrain.kupidon.util.extension.respondInvalidInputBody
import com.rrain.kupidon.util.extension.respondNoUser
import com.rrain.kupidon.util.extension.use
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
          val user = user.toMapToSend()
        })
      }
    }
    
    
    
    
    
    
    get(UserRoutes.getById) {
      val user = try { userServ.getById(call.parameters["id"]!!) }
      catch (ex: Exception){
        null
      }
      
      user ?: return@get call.respond(
        HttpStatusCode.BadRequest,
        object{ val user = null }
      )
      
      
      return@get call.respond(object {
        val user = user.toMapToSend()
      })
    }
    
    
    
    
    
    
    data class UserCreateReq(
      val email: String,
      val pwd: String,
      val name: String,
      val gender: Gender,
      val birthDate: ZonedDateTime
    )
    post(UserRoutes.create) {
      val userToCreate = try {
        call.receive<UserCreateReq>()
      } catch (ex: Exception){
        return@post call.respondInvalidInputBody()
      }
      
      if (!userToCreate.email.matches(Regex("^.+@.+$"))){
        return@post call.respondInvalidInputBody(
          "Invalid email format"
        )
      }
      if (userToCreate.email.length>100)
        return@post call.respondInvalidInputBody(
          "Email max length is 100 chars"
        )
      
      if (userToCreate.pwd.length<6){
        return@post call.respondInvalidInputBody(
          "Password must be at least 6 chars length"
        )
      }
      if (userToCreate.pwd.length>200)
        return@post call.respondInvalidInputBody(
          "Password max length is 200 chars"
        )
      
      if (userToCreate.name.isEmpty()){
        return@post call.respondInvalidInputBody(
          "Name must not be empty"
        )
      }
      if (userToCreate.name.length>100){
        return@post call.respondInvalidInputBody(
          "Name max length is 100"
        )
      }
      
      val nowWithUserZone = zonedNow()
        .withZoneSameInstant(userToCreate.birthDate.zone)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
      if (ChronoUnit.YEARS.between(userToCreate.birthDate, nowWithUserZone)<18){
        return@post call.respondInvalidInputBody(
          "You must be at least 18 years old"
        )
      }
      
      
      val tryUser = User(
        email = userToCreate.email,
        pwd = userToCreate.pwd.let(PwdHashing::generateHash),
        name = userToCreate.name,
        gender = userToCreate.gender,
        birthDate = userToCreate.birthDate.toLocalDate(),
      )
      
      
      val conn = userServ.pool.create().awaitSingle()
      conn.transactionIsolationLevel = IsolationLevel.SERIALIZABLE
      val user = conn.use {
        val userByEmail = userServ.getByEmail(tryUser.email!!, conn)
        
        if (userByEmail!=null) return@post call.respondBadRequest(
          code = "DUPLICATE_EMAIL",
          msg = "User with such email already exists",
        )
        
        userServ.create(tryUser, conn)
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
      call.respond(object {
        val accessToken = accessToken
        val user = user.toMapToSend()
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
        
        val colToValue = mutableMapOf<Column,Any?>()
        lateinit var currentPwd: String
        
        dataAsMap.forEach { (k,v) -> when(k){
          
          "name" -> {
            try {
              if (v !is String) throw RuntimeException()
              if (v.isEmpty()) throw RuntimeException()
              if (v.length>100) throw RuntimeException()
              colToValue[UserTname] = v
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "Name must be string and must not be empty and name max length is 100"
              )
            }
          }
          
          "birthDate" -> {
            try {
              if (v !is String) throw RuntimeException()
              val birthDate = v.toZonedDateTime()
              val nowWithUserZone = zonedNow()
                .withZoneSameInstant(birthDate.zone)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
              if (ChronoUnit.YEARS.between(birthDate, nowWithUserZone)<18){
                return@put call.respondInvalidInputBody(
                  "You must be at least 18 years old"
                )
              }
              colToValue[UserTbirthDate] = birthDate.toLocalDate()
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "'birthDate' must be string '$zonedDateTimePattern'" +
                  ", for example '2005-11-10T00:00:00.000+08:00'"
              )
            }
          }
          
          "gender" -> {
            try {
              if (v !is String) throw RuntimeException()
              val gender = Gender.valueOf(v)
              colToValue[UserTgender] = gender
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "Gender must be string of 'MALE' | 'FEMALE'"
              )
            }
          }
          
          "aboutMe" -> {
            try {
              if (v !is String) throw RuntimeException()
              if (v.length>2000) throw RuntimeException()
              colToValue[UserTaboutMe] = v
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "'About me' must be string and must have max 2000 chars"
              )
            }
          }
          
          "currentPwd" -> {
            try{
              if (v !is String) throw RuntimeException()
              if (v.length<1 || v.length>200) throw RuntimeException()
              currentPwd = v.let(PwdHashing::generateHash)
            } catch (ex: Exception) {
              return@put call.respondInvalidInputBody(
                "Current password must be string and its length must be from 1 to 200 chars"
              )
            }
          }
          
          "pwd" -> {
            try{
              if (v !is String) throw RuntimeException()
              if (v.length<6 || v.length>200) throw RuntimeException()
              colToValue[UserTpwd] = v.let(PwdHashing::generateHash)
            } catch (ex: Exception) {
              return@put call.respondInvalidInputBody(
                "Password must be string and its length must be from 6 to 200 chars"
              )
            }
          }
          
          else -> {
            return@put call.respondInvalidInputBody(
              "Unknown property '$k'"
            )
          }
        }}
        
        
        val conn = userServ.pool.create().awaitSingle()
        conn.transactionIsolationLevel = IsolationLevel.SERIALIZABLE
        val user = conn.use {
          val user = userServ.getById(userId, conn)
          
          user ?: return@put call.respondNoUser()
          
          if (user.pwd!=currentPwd) {
            return@put call.respondBadRequest(
              "INVALID_PWD",
              "Invalid current password"
            )
          }
          
          userServ.update(userId, colToValue, conn)
        }
        
        
        call.respond(object {
          val user = user.toMapToSend()
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
      conn.transactionIsolationLevel = IsolationLevel.SERIALIZABLE
      conn.use {
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
          mapOf(UserTemailVerified to true),
          conn
        )
      }
      
      return@get call.respondText(
        status = HttpStatusCode.OK,
        text = getHtmlResponse("Успешно завершено! Ваша почта успешно верифицирована!"),
        contentType = ContentType.Text.Html
      )
    }
    
    
  }
  
}