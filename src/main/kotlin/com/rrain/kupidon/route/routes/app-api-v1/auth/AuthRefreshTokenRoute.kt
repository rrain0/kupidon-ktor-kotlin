package com.rrain.kupidon.route.routes.app.api.v1.auth

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.*
import com.rrain.kupidon.service.JwtService.sessionId
import com.rrain.kupidon.service.JwtService.userId
import com.rrain.kupidon.service.mongo.findUserById
import com.rrain.`util-ktor`.call.host
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addAuthRefreshTokensRoute() {
  routing {
    get(ApiV1Routes.authRefreshTokens) {
      val refreshToken = call.request.cookies[JwtService.config.refreshTokenCookieName]
      
      refreshToken ?: return@get call.respondBadRequest(
        code = "NO_REFRESH_TOKEN_COOKIE",
        msg = "No refresh token cookie",
      )
      
      val verifier = JwtService.refreshTokenVerifier
      val decodedRefresh =
        try { verifier.verify(refreshToken) }
        catch (ex: AlgorithmMismatchException) {
          return@get call.respondBadRequest(ErrTokenAlgorithmMismatch)
        }
        catch (ex: JWTDecodeException) {
          return@get call.respondBadRequest(ErrTokenDamaged)
        }
        catch (ex: SignatureVerificationException) {
          return@get call.respondBadRequest(ErrTokenModified)
        }
        catch (ex: TokenExpiredException) {
          return@get call.respondBadRequest(ErrTokenExpired)
        }
        catch (ex: MissingClaimException) {
          return@get call.respondBadRequest(ErrTokenLacksOfClaim)
        }
        catch (ex: Exception) {
          ex.printStackTrace()
          return@get call.respondBadRequest(ErrTokenUnknownVerificationError)
        }
      
      
      val user = findUserById(decodedRefresh.userId)
      user ?: return@get call.respondNoUserById()
      
      val updatedSession = JwtLoginService.login(user.id, user.roles, decodedRefresh.sessionId)
      
      call.response.cookies.append(
        JwtService.getRefreshTokenCookie(updatedSession.refreshToken, call.host)
      )
      call.respond(mapOf(
        "accessToken" to updatedSession.accessToken,
      ))
    }
  }
}
