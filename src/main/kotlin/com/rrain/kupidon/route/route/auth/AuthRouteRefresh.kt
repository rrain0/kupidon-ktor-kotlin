package com.rrain.kupidon.route.route.auth

import com.auth0.jwt.exceptions.*
import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondNoUser
import com.rrain.kupidon.service.*
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.util.toUuid
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList



fun Application.configureAuthRouteRefresh(){
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    
    
    get(AuthRoutes.refresh){
      val refreshToken = call.request.cookies[JwtService.refreshTokenCookieName]
      
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
      
      
      val id = decodedRefresh.subject
      val user = mongo().db.coll<UserMongo>("users")
        .find(Filters.eq(UserMongo::id.name, id.toUuid()))
        .toList().firstOrNull()
      
      user ?: return@get call.respondNoUser()
      
      val roles = user.roles
      val domain = call.request.origin.serverHost
      
      val newAccessToken = JwtService.generateAccessToken(id, roles)
      val newRefreshToken = JwtService.generateRefreshToken(id)
      
      // сделать позже save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(newRefreshToken,domain)
      )
      call.respond(object {
        val accessToken = newAccessToken
      })
    }
    
    
    
  }
  
}
