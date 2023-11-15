package com.rrain.kupidon.routes

import com.auth0.jwt.exceptions.*
import com.rrain.kupidon.service.DatabaseService
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.service.TokenError
import com.rrain.kupidon.util.extension.respondInvalidInputBody
import com.rrain.kupidon.util.extension.respondNoUser
import io.ktor.http.*
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
  
  val userServ = DatabaseService.userServ
  
  routing {
    
    
    
    
    data class LoginRequest(val login: String, val pwd: String)
    post(AuthRoutes.login) {
      val loginRequest = try {
        call.receive<LoginRequest>()
      } catch (ex: Exception){
        return@post call.respondInvalidInputBody()
      }
      val login = loginRequest.login
      val pwd = loginRequest.pwd
      
      
      val user = userServ.getByEmail(login)
      user ?: return@post call.respond(HttpStatusCode.BadRequest, object {
        val code = "NO_USER"
        val msg = "There is no user with such login-password"
      })
      
      if (!PwdHashing.checkPwd(pwd, user.pwd!!))
        return@post call.respond(HttpStatusCode.BadRequest, object {
          val code = "NO_USER"
          val msg = "There is no user with such login-password"
        })
      
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
      
      refreshToken ?: return@get call.respond(
        HttpStatusCode.BadRequest,
        object {
          val code = "NO_REFRESH_TOKEN_COOKIE"
          val msg = "No refresh token cookie"
        }
      )
      
      val verifier = JwtService.refreshTokenVerifier
      val decodedRefresh = try { verifier.verify(refreshToken) }
      // Token was encoded by wrong algorithm. Required HMAC256.
      catch (ex: AlgorithmMismatchException) { return@get call.respond(
        HttpStatusCode.BadRequest, object {
          val code = TokenError.TOKEN_ALGORITHM_MISMATCH.name
          val msg = TokenError.TOKEN_ALGORITHM_MISMATCH.msg
        }
      )}
      // Damaged Token - Токен повреждён и не может быть декодирован
      catch (ex: JWTDecodeException) { return@get call.respond(
        HttpStatusCode.BadRequest, object {
          val code = TokenError.TOKEN_DAMAGED.name
          val msg = TokenError.TOKEN_DAMAGED.msg
        }
      )}
      // Modified Token - Токен умышленно модифицирован (подделан)
      catch (ex: SignatureVerificationException) { return@get call.respond(
        HttpStatusCode.BadRequest, object {
          val code = TokenError.TOKEN_MODIFIED.name
          val msg = TokenError.TOKEN_MODIFIED.msg
        }
      )}
      // Token has expired
      catch (ex: TokenExpiredException) { return@get call.respond(
        HttpStatusCode.BadRequest,
        object {
          val code = TokenError.TOKEN_EXPIRED.name
          val msg = TokenError.TOKEN_EXPIRED.msg
        }
      )}
      // Common Verification Exception
      catch (ex: JWTVerificationException) {
        ex.printStackTrace()
        return@get call.respond(
          HttpStatusCode.BadRequest, object {
            val code = TokenError.UNKNOWN_VERIFICATION_ERROR.name
            val msg = TokenError.UNKNOWN_VERIFICATION_ERROR.msg
          }
        )
      }
      
      
      val id = decodedRefresh.subject
      val user = userServ.getById(id)
      
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
