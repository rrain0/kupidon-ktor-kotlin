package com.rrain.kupidon.routes.test

import com.rrain.kupidon.util.printHeaders
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureHttpTestRoutes(){
  
  routing {
    
    get("/test/http/headers"){
      val headers = call.printHeaders()
      call.respond(headers)
    }
    
    get("/test/http/proxy-info"){
      
      // https://ktor.io/docs/forward-headers.html#original-request-information
      
      // this info automatically uses forwarded http headers (Forwarded / X-Forwarded-...)
      val info = mapOf(
        // get information about the proxy request
        // proxy nginx -> this server
        "local" to "proxy nginx -> ktor server",
        
        "localHostLocal" to call.request.local.localHost,
        "localHostServer" to call.request.local.serverHost,
        "localHostRemote" to call.request.local.remoteHost,
        
        "localPortLocal" to call.request.local.localPort,
        "localPortServer" to call.request.local.serverPort,
        "localPortRemote" to call.request.local.remotePort,
        
        // get information about the original request
        // client -> proxy nginx
        "origin" to "client -> proxy nginx",
        
        "originHostLocal" to call.request.origin.localHost,
        "originHostServer" to call.request.origin.serverHost,
        "originHostRemote" to call.request.origin.remoteHost,
        
        "originPortLocal" to call.request.origin.localPort,
        "originPortServer" to call.request.origin.serverPort,
        "originPortRemote" to call.request.origin.remotePort,
      )
      
      println("Test HTTP proxy info: $info")
      
      call.respond(info)
    }
    
  }
  
}