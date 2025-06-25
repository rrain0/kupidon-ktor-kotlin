package com.rrain.kupidon.route.routes.app.api.v1.auth

import com.auth0.jwt.exceptions.*
import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.*
import com.rrain.kupidon.service.JwtService.getUserId
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.kupidon.service.mongo.model.UserProfilePhotoM
import com.rrain.kupidon.service.mongo.model.projectionUserM
import com.rrain.util.uuid.toUuid
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull



fun Application.addAuthRefreshTokensRoute() {
  routing {
    get(ApiV1Routes.authRefreshTokens) {
      val refreshToken = call.request.cookies[JwtService.config.refreshTokenCookieName]
      
      refreshToken ?: return@get call.respondBadRequest(
        code = "NO_REFRESH_TOKEN_COOKIE",
        msg = "No refresh token cookie",
      )
      
      val verifier = JwtService.refreshTokenVerifier
      val decodedRefresh = try { verifier.verify(refreshToken) }
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
      catch (ex: JWTVerificationException) {
        ex.printStackTrace()
        return@get call.respondBadRequest(ErrTokenUnknownVerificationError)
      }
      
      
      val userUuid = decodedRefresh.getUserId().toUuid()
      
      val nUserId = UserM::id.name
      val nUserPhotos = UserM::photos.name
      val nPhotoBinData = UserProfilePhotoM::binData.name
      
      val user = collUsers
        .find(Filters.eq(nUserId, userUuid))
        .projectionUserM()
        .firstOrNull()
      
      user ?: return@get call.respondNoUserById()
      
      val roles = user.roles
      val domain = call.request.origin.serverHost
      
      val newAccessToken = JwtService.generateAccessToken(userUuid.toString(), roles)
      val newRefreshToken = JwtService.generateRefreshToken(userUuid.toString())
      
      // 1) Сделать позже - save refresh token & device info to db as opened session
      // 2) При генерации access token генерится и новый refresh token, а старые рефреши всё ещё валидны
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(newRefreshToken,domain)
      )
      call.respond(mapOf(
        "accessToken" to newAccessToken
      ))
    }
  }
}
