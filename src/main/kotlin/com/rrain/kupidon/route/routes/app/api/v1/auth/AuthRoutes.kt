package com.rrain.kupidon.route.routes.app.api.v1.auth

import com.rrain.kupidon.route.routes.app.api.v1.ApiV1Routes
import io.ktor.server.application.*
import io.ktor.server.routing.*



object AuthRoutes {
  const val base = "${ApiV1Routes.base}/auth"
  const val login = "$base/login"
  const val refresh = "$base/refresh"
  //const val logout = "$base/logout"
}




fun Application.configureAuthRoutes(){
  configureAuthRouteLogin()
  configureAuthRouteRefresh()
  
  
  
  
  routing {
    
    
    
    
    /*
    delete(AuthRoutes.logout) {
      
      // сделать позже remove refresh/access tokens from database
      
      val domain = call.request.origin.serverHost
      
      // generate expired cookie
      call.response.cookies.append(
        JwtService.generateRefreshTokenExpiredCookie(domain)
      )
      
      call.respond(HttpStatusCode.OK)
    }
    */
    
    
  }
  
}
