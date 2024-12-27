package com.rrain.kupidon.route.routes.test

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.plugin.authorized
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.configureAuthorizationTestRoutes(){
  
  routing {
    
    authenticate {
      authorized(Role.ADMIN) {
        get("/test/auth/for-admins") {
          call.respondText("you are admin")
        }
      }
    }
    
  }
  
}