package com.rrain.kupidon.route

import com.rrain.kupidon.route.route.auth.configureAuthRoutes
import com.rrain.kupidon.route.route.test.configureStaticRoutes
import com.rrain.kupidon.route.route.main.configureMainRoutes
import com.rrain.kupidon.route.route.`pwa-manifest`.configurePwaManifestRoute
import com.rrain.kupidon.route.route.test.*
import com.rrain.kupidon.route.route.user.configureUserRoutes
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.resources.*
import io.ktor.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.autohead.*
import io.ktor.server.application.*



fun Application.configureRouting() {
  
  install(Resources)
  
  install(AutoHeadResponse)
  
  
  
  routing {
  
  
  }
  
  
  
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


