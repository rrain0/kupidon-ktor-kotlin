package com.rrain.kupidon.route.route.auth

import com.mongodb.client.model.Filters
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondInvalidBody
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList



fun Application.configureAuthRouteLogin(){
  
  fun mongo() = MongoDbService.client
  
  
  
  routing {
    
    
    
    
    data class LoginRequest(
      val login: String,
      val pwd: String
    )
    post(AuthRoutes.login) {
      val login =
      try { call.receive<LoginRequest>() }
      catch (ex: Exception){
        return@post call.respondInvalidBody()
      }
      
      val user = mongo().db.coll<UserMongo>("users")
        .find(Filters.eq(UserMongo::email.name, login.login))
        .toList().firstOrNull()
      
      if (user==null || !PwdHashing.checkPwd(login.pwd, user.pwd))
        return@post call.respondBadRequest(
          code = "NO_USER",
          msg = "There is no user with such login-password",
        )
      
      val id = user.id.toString()
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
        val user = user.convertToSend(call.request)
      })
    }
    
    
    
    
    
    
  }
  
}
