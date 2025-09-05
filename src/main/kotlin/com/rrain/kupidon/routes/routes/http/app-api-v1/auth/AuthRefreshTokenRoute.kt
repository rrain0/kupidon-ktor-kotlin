package com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon.routes.`response-errors`.respondBadRequest
import com.rrain.kupidon.routes.`response-errors`.respondNoUserById
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.jwt.ErrTokenAlgorithmMismatch
import com.rrain.kupidon.services.jwt.ErrTokenDamaged
import com.rrain.kupidon.services.jwt.ErrTokenExpired
import com.rrain.kupidon.services.jwt.ErrTokenLacksOfClaim
import com.rrain.kupidon.services.jwt.ErrTokenModified
import com.rrain.kupidon.services.jwt.ErrTokenUnknownVerificationError
import com.rrain.kupidon.services.jwt.JwtService
import com.rrain.kupidon.services.jwt.RefreshToken
import com.rrain.kupidon.services.jwt.refreshTokenCookie
import com.rrain.kupidon.services.login.JwtLoginService
import com.rrain.kupidon.services.mongo.findUserById
import com.rrain.utils.ktor.call.host
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addAuthRefreshTokensRoute() {
  routing {
    get(ApiV1Routes.authRefreshTokens) {
      val refreshToken = call.refreshTokenCookie
      
      refreshToken ?: return@get call.respondBadRequest(
        code = "NO_REFRESH_TOKEN_COOKIE",
        msg = "No refresh token cookie",
      )
      
      val decodedRefresh =
        try {
          RefreshToken(refreshToken)
        }
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
