package com.rrain.kupidon.route.route.user

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon._old.v03.postgres.service.db.table.*
import com.rrain.kupidon._old.v03.postgres.service.db.PostgresDbService.userServ
import com.rrain.kupidon._old.v03.postgres.service.db.use
import com.rrain.kupidon.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language



fun Application.configureUserRouteEmailInitialVerify() {
  
  
  
  routing {
    
    
    /*
    // TODO Language
    get(UserRoutes.emailInitialVerification) {
      val verificationToken = call.request.queryParameters[UserRoutes.verifyTokenParamName]
      
      fun getHtmlResponse(msg: String): String {
        @Language("html") val html = """
          <html lang="ru-RU">
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