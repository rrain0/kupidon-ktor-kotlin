package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.util.respondNoUser
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.util.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList


object UserRoutes {
  const val base = "/api/user"
  const val current = "$base/current"
  const val create = "$base/create"
  const val update = "$base/update"
  const val emailInitialVerification = "$base/verify/initial-email"
  const val getById = "$base/get-by-id/{id}"
  
  const val verifyTokenParamName = "verificationToken"
}



fun Application.configureUserRoutes(){
  configureUserRouteCreate()
  configureUserRouteUpdate()
  
  
  fun mongo() = MongoDbService.client
  
  
  
  routing {
    
    
    
    
    
    authenticate {
      get(UserRoutes.current) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        val userById = mongo().db.coll<UserMongo>("users")
          .find(Filters.eq(UserMongo::id.name, userId.toUuid()))
          .toList().firstOrNull()
        userById ?: return@get call.respondNoUser()
        call.respond(object {
          val user = userById.toMapToSend()
        })
      }
    }
    
    
    
    
    
    
    get(UserRoutes.getById) {
      val userId = call.parameters["id"]!!
      val userById = mongo().db.coll<UserMongo>("users")
        .find(Filters.eq(UserMongo::id.name, userId.toUuid()))
        .toList().firstOrNull()
      
      userById ?: return@get call.respond(
        HttpStatusCode.BadRequest,
        object{ val user = null }
      )
      
      
      return@get call.respond(object {
        val user = userById.toMapToSend()
      })
    }
    
    
    
    
    
    
    /*
    // todo Language
    get(UserRoutes.emailInitialVerification) {
      val verificationToken = call.request.queryParameters[UserRoutes.verifyTokenParamName]
      
      fun getHtmlResponse(msg: String): String {
        @Language("html") val html = """
          <html lang="ru">
          <head>
            <meta charset="utf-8">
            <title>Верификация почты в приложении Купидон</title>
          </head>
          <body>
            <p style="font-size: 2em;">${msg}</p>
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
    */
    
    
    
  }
  
}