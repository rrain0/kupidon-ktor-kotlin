package com.rrain.kupidon.route.routes.`app-api-v1`.auth

import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.*




fun Application.addAuthOtherRoutes() {
  
  /*
  routing {
    
    delete("${ApiV1Routes.auth}/logout") {
      
      // сделать позже remove refresh/access tokens from database
      
      val domain = call.request.origin.serverHost
      
      // generate expired cookie
      call.response.cookies.append(
        JwtService.generateExpiredRefreshTokenCookie(domain)
      )
      
      call.respond(HttpStatusCode.OK)
    }
    
  }
  */
}
