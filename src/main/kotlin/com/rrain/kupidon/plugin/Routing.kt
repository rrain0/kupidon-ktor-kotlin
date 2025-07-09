package com.rrain.kupidon.plugin

import io.ktor.server.application.*
import io.ktor.server.routing.IgnoreTrailingSlash



fun Application.configureRouting() {
  
  install(IgnoreTrailingSlash)
  
}


