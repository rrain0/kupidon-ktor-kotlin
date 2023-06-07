package com.rrain.kupidon.routes

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.rrain.kupidon.service.DatabaseService
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.util.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.NoSuchElementException


val authBaseRoute = "/api/auth"
val loginRoute = "$authBaseRoute/login"
val refreshRoute = "$authBaseRoute/refresh"
val logoutRoute = "$authBaseRoute/logout"



class NoSuchUserException(msg: String): RuntimeException(msg)

fun Application.configureAuthRoutes(){
  
  val appConfig = environment.config
  
  val userServ = DatabaseService.userServ
  
  routing {
    
    class AccessTokenResponse(val accessToken: String)
    
    data class LoginRequest(val login: String?, val pwd: String?)
    post(loginRoute) {
      val loginRequest = call.receive<LoginRequest>()
      
      // TODO validation layer for form fields
      val login = loginRequest.login!!
      val pwd = loginRequest.pwd!!
      
      val user = userServ.getByEmail(login)
        ?: throw NoSuchUserException("There is no user with such login-password")
      
      if (!PwdHashing.checkPwd(pwd, user.pwd!!))
        throw NoSuchUserException("There is no user with such login-password")
      
      val id = user.id!!
      val roles = user.roles
      
      val domain = call.request.origin.serverHost
      
      val accessToken = JwtService.generateAccessToken(id, roles)
      val refreshToken = JwtService.generateRefreshToken(id)
      
      // todo save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(refreshToken,domain)
      )
      
      call.respond(AccessTokenResponse(accessToken))
    }
    
    
    put(refreshRoute){
      call.request.cookies[JwtService.refreshTokenCookieName]?.let { refreshToken ->
        val refreshJwtSecret = appConfig["jwt.refresh-token.secret"]
        val verifier = JwtService.getRefreshTokenVerifier(refreshJwtSecret)
        val decodedJwt = try {
          verifier.verify(refreshToken)
        } catch (signatureEx: SignatureVerificationException){
          throw signatureEx
        } catch (algorithmEx: AlgorithmMismatchException){
          throw algorithmEx
        } catch (expiredEx: TokenExpiredException){
          throw expiredEx
        }
        
        val id = decodedJwt.subject
        val user = try {
          userServ.getById(id)
            ?: throw RuntimeException("Пользователь с таким id не найден")
        } catch (ex: NoSuchElementException){
          throw RuntimeException("Пользователь с таким id не найден")
        }
        val roles = user.roles
        
        val domain = call.request.origin.serverHost
        
        val newAccessToken = JwtService.generateAccessToken(id, roles)
        val newRefreshToken = JwtService.generateRefreshToken(id)
        
        // todo save refresh token & device info to db as opened session
        
        call.response.cookies.append(
          JwtService.generateRefreshTokenCookie(newRefreshToken,domain)
        )
        
        call.respond(AccessTokenResponse(newAccessToken))
      }
      
      call.respond(HttpStatusCode.BadRequest)
    }
    
    
    delete(logoutRoute) {
      
      val domain = call.request.origin.serverHost
      
      // generate expired cookie
      call.response.cookies.append(
        JwtService.generateRefreshTokenExpiredCookie(domain)
      )
      
      call.respond(HttpStatusCode.OK)
    }
    
    
  }
  
}
