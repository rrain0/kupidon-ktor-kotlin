package com.rrain.kupidon.route.routes.http.main


import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*




fun Application.addMainRoutes() {
  routing {
    
    get("/ktor/hello") {
      call.respondText("Hello Ktor!")
    }
    
  }
}