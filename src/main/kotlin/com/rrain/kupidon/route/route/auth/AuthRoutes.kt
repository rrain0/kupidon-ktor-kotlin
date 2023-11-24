package com.rrain.kupidon.route.route.auth

import io.ktor.server.application.*
import io.ktor.server.routing.*



object AuthRoutes {
  const val base = "/api/auth"
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
