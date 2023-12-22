package com.rrain.kupidon._old.v03.postgres.route.auth

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon._old.v03.postgres.service.db.PostgresDbService
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondInvalidBody
import com.rrain.kupidon.route.util.respondNoUser
import com.rrain.kupidon.service.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



object AuthRoutes {
  const val base = "/api/auth"
  const val login = "$base/login"
  const val refresh = "$base/refresh"
  //const val logout = "$base/logout"
}




fun Application.configureAuthRoutes(){
  
  val userServ = PostgresDbService.userServ
  
  routing {
    
    
    
    
    data class LoginRequest(val login: String, val pwd: String)
    post(AuthRoutes.login) {
      val loginRequest = try {
        call.receive<LoginRequest>()
      } catch (ex: Exception){
        return@post call.respondInvalidBody()
      }
      val login = loginRequest.login
      val pwd = loginRequest.pwd
      
      
      val user = userServ.getByEmail(login)
      
      
      if (user==null || !PwdHashing.checkPwd(pwd, user.pwd!!))
        return@post call.respondBadRequest(
          code = "NO_USER",
          msg = "There is no user with such login-password",
        )
      
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
        val user = user.toMapToSend()
      })
    }
    
    
    
    
    
    get(AuthRoutes.refresh){
      
      val refreshToken = call.request.cookies[JwtService.refreshTokenCookieName]
      
      refreshToken ?: return@get call.respondBadRequest(
        code = "NO_REFRESH_TOKEN_COOKIE",
        msg = "No refresh token cookie",
      )
      
      val verifier = JwtService.refreshTokenVerifier
      val decodedRefresh = try { verifier.verify(refreshToken) }
      catch (ex: AlgorithmMismatchException) {
        return@get call.respond(ErrTokenAlgorithmMismatch)
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
      val user = userServ.getById(id)
      
      user ?: return@get call.respondNoUser()
      
      val roles = user.roles
      
      val domain = call.request.origin.serverHost
      
      val newAccessToken = JwtService.generateAccessToken(id,roles)
      val newRefreshToken = JwtService.generateRefreshToken(id)
      
      // сделать позже save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(newRefreshToken,domain)
      )
      call.respond(object {
        val accessToken = newAccessToken
      })
    }
    
    
    /*
    delete(AuthRoutes.logout) {
      
      // сделать позже remove refresh/access tokens from database
      
      val domain = call.request.origin.serverHost
      
      // generate expired cookie
      call.response.cookies.append(
        JwtService.generateRefreshTokenExpiredCookie(domain)
      )
      
      call.respond(HttpStatusCode.OK)
    }
    */
    
    
  }
  
}
