package com.rrain.kupidon.routes

import com.rrain.kupidon.plugins.authenticationPluginName
import com.rrain.kupidon.service.DatabaseService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking


fun Application.configureUserRoutes(){
  
  val userServ = DatabaseService.userServ
  
  routing {
    
    authenticate(authenticationPluginName) {
      get("/api/user/current") {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        //val username = principal.payload.getClaim("username").asString()
        val user = userServ.getById(userId)!!
        call.respond(object {
          val user = object {
            val id = user.id
            val email = user.email
            val roles = user.roles
            val nickname = user.nickname
            val subnickname = user.subnickname
            val created = user.created
            val updated = user.updated
            val firstName = user.firstName
            val lastName = user.lastName
            val birthDate = user.birthDate
          }
        })
      }
    }
    
    get("/api/user/getById/{id}") {
      call.respond( object {
        val user = runBlocking { userServ.getById(call.parameters["id"]!!) }
      } )
    }
    
  }
  
  
}