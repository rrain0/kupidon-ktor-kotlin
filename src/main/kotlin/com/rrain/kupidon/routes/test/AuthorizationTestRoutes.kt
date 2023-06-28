package com.rrain.kupidon.routes.test

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.plugins.withAnyRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureAuthorizationTestRoutes(){
  
  routing {
    authenticate {
      withAnyRole(Role.ADMIN) {
        get("/test/auth/for-admins") {
          call.respondText("you are admin")
        }
      }
    }
  }
  
}