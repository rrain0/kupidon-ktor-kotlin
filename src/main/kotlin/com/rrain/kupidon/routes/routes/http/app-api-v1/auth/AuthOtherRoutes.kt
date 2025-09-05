package com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth

import io.ktor.server.application.*




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
