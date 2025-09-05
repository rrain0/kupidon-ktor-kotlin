package com.rrain.kupidon.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.IgnoreTrailingSlash



fun Application.configureRouting() {
  
  install(IgnoreTrailingSlash)
  
}


