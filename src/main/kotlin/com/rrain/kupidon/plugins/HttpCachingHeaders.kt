package com.rrain.kupidon.plugins

import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureHttpCachingHeaders() {
  
  routing {
    // I use cache control directly in endpoints, but plugin needs to be installed
    install(CachingHeaders)
  }
  
}
