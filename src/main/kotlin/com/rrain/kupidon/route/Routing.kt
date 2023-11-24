package com.rrain.kupidon.route

import com.rrain.kupidon.route.route.auth.configureAuthRoutes
import com.rrain.kupidon.route.route.main.configureMainRoutes
import com.rrain.kupidon.route.route.`pwa-manifest`.configurePwaManifestRoute
import com.rrain.kupidon.route.route.test.*
import com.rrain.kupidon.route.route.user.configureUserRoutes
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
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
    
    // todo explore static resources
    // Static plugin. Try to access `/static/index.html`
    static("/static") {
      resources("static")
    }
    
    get<Articles> { article ->
      // Get all articles ...
      call.respond("List of articles sorted starting from ${article.sort}")
    }
  }
  
  
  
  
  configureTestRoutes()
  configureMainRoutes()
  configurePwaManifestRoute()
  configureAuthRoutes()
  configureUserRoutes()
  
}



@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
