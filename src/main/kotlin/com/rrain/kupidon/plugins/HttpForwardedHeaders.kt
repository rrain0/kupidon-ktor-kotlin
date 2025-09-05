package com.rrain.kupidon.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*


fun Application.configureHttpForwardedHeaders() {
  
  
  // Use proxy server forwarded & x-forwarded headers
  // WARNING: for security, do not include this if not behind a reverse proxy
  // For nginx there is sufficient only install(XForwardedHeaders)
  install(XForwardedHeaders)
  //install(ForwardedHeaders)
  
  
}
