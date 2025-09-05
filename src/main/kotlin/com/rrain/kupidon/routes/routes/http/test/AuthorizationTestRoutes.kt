package com.rrain.kupidon.routes.routes.http.test

import com.rrain.kupidon.models.Permission
import com.rrain.kupidon.plugins.authorize
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addAuthorizationTestRoutes() {
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