package com.rrain.kupidon.plugins

import io.ktor.server.plugins.partialcontent.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*


fun Application.configureHTTP() {
  
  // todo explore
  install(PartialContent) {
    // Maximum number of ranges that will be accepted from an HTTP request.
    // If the HTTP request specifies more ranges, they will all be merged into a single range.
    maxRangeCount = 10
  }
  
  
  
  /*
    CORS - Cross-Origin Resource Sharing
    CORS response will be sent on preflight request
   */
  install(CORS) {
    
    allowCredentials = true
    
    allowMethod(HttpMethod.Get) // Fetches the state of a resource without altering the system
    allowMethod(HttpMethod.Post) // Creates a new resource without saying where
    allowMethod(HttpMethod.Put) // Replaces an existing resource, overwriting whatever else (if anything) is already there
    allowMethod(HttpMethod.Delete) // Removes an existing resource
    allowMethod(HttpMethod.Patch) // Alters an existing resource (partially rather than creating a new resource)
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Head)
    
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
    //allowHeader("MyCustomHeader") // allow custom header
    
    maxAgeInSeconds = 2 * 60 * 60 // results of preflight request will be valid for 2 hours
    
    // development
    allowHost("dev.kupidon.rrain.ydns.eu:50030", listOf("https"))
    allowHost("localhost:50030", listOf("http","https"))
    allowHost("192.168.0.178:50030", listOf("http","https"))
    
    // production
    allowHost("kupidon.rrain.ydns.eu", listOf("https"))
  }
  
  
  // use proxy server forwarded headers
  install(XForwardedHeaders)
  
  
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
