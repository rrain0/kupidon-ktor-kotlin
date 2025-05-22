package com.rrain.kupidon.route

import com.rrain.kupidon.route.routes.app.api.v1.auth.configureAuthRoutes
import com.rrain.kupidon.route.routes.main.configureMainRoutes
import com.rrain.kupidon.route.routes.app.`pwa-manifest`.configurePwaManifestRoute
import com.rrain.kupidon.route.routes.test.*
import com.rrain.kupidon.route.routes.app.api.v1.user.configureUserRoutes
import com.rrain.kupidon.route.routes.app.api.v1.`users-list`.configureUsersListRoutes
import io.ktor.server.application.*



fun Application.configureRouting() {
  
  configureTestRoutes()
  configureMainRoutes()
  configurePwaManifestRoute()
  configureAuthRoutes()
  configureUserRoutes()
  configureUsersListRoutes()
  
}


