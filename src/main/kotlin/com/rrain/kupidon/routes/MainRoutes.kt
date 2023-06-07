package com.rrain.kupidon.routes

import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.resources.*
import io.ktor.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.autohead.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureMainRoutes(){
  
  routing {
    get("/ktor/hello") {
      call.respondText("Hello World!")
    }
  }
  
}