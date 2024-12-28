package com.rrain.kupidon.route

import com.rrain.kupidon.route.routes.auth.configureAuthRoutes
import com.rrain.kupidon.route.routes.main.configureMainRoutes
import com.rrain.kupidon.route.routes.`pwa-manifest`.configurePwaManifestRoute
import com.rrain.kupidon.route.routes.test.*
import com.rrain.kupidon.route.routes.user.configureUserRoutes
import io.ktor.server.application.*



fun Application.configureRouting() {
  
  configureTestRoutes()
  configureMainRoutes()
  configurePwaManifestRoute()
  configureAuthRoutes()
  configureUserRoutes()
  
  
  /*routing {
    get(
      "{...}" // Fallback route handler if no match
    ){
      call.respond("no match")
    }
  }*/
  
}


