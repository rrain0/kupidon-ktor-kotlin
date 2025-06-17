package com.rrain.kupidon.route.routes.test

import com.rrain.kupidon.model.Permission
import com.rrain.kupidon.plugin.authorize
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.configureAuthorizationTestRoutes() {
  
  routing {
    
    authenticate {
      authorize(Permission.ADMIN) {
        get("/test/require-admin-permission") {
          call.respondText("You have '${Permission.ADMIN}' permission")
        }
      }
    }
    
  }
  
}