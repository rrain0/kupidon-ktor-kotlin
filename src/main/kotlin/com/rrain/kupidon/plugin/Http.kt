package com.rrain.kupidon.plugin

import io.ktor.server.plugins.partialcontent.*
import io.ktor.http.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*


fun Application.configureHttp() {
  
  
  // todo explore
  install(PartialContent) {
    // Maximum number of ranges that will be accepted from an HTTP request.
    // If the HTTP request specifies more ranges, they will all be merged into a single range.
    maxRangeCount = 10
  }
  
  
  
  // Use proxy server forwarded & x-forwarded headers
  // WARNING: for security, do not include this if not behind a reverse proxy
  install(XForwardedHeaders)
  //install(ForwardedHeaders)
  
  
  
  // todo explore
  install(CachingHeaders) {
    options { call, outgoingContent ->
      when (outgoingContent.contentType?.withoutParameters()) {
        ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
        else -> null
      }
    }
  }
  
  
  
}
