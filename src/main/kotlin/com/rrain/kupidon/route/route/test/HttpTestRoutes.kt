package com.rrain.kupidon.route.route.test

import com.rrain.kupidon.util.printHeaders
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.configureHttpTestRoutes(){
  
  routing {
    
    get("/test/params"){
      // get only first occurrence
      val param = call.parameters["param"]
      // get all occurrences as list
      val array = call.parameters.getAll("array")
      call.respond(object {
        val param = param
        val arrayParam = array
      })
      /*
      Example: /test/params?param=sdf,j&param=h,hh&array=firs,t&array=secon,d
      Result: {
        "param" : "sdf,j",
        "arrayParam" : [ "firs,t", "secon,d" ]
      }
      */
    }
    
    get("/test/http/headers"){
      val headers = call.printHeaders()
      call.respond(headers)
      
      
      // Set headers: https://ktor.io/docs/responses.html#file
      //call.response.header(HttpHeaders.ContentType, "application/json")
    }
    
    get("/test/http/proxy-info"){
      
      // https://ktor.io/docs/forward-headers.html#original-request-information
      
      // this info automatically uses forwarded http headers (Forwarded / X-Forwarded-...)
      val info = mapOf(
        // get information about the original request
        // client -> proxy nginx
        "origin" to "client -> proxy nginx",
        
        "originHostLocal" to call.request.origin.localHost, // "127.0.0.1" "[0:0:0:0:0:0:0:1]"
        "originHostServer" to call.request.origin.serverHost, // "kupidon.ddns.net"
        "originHostRemote" to call.request.origin.remoteHost, // "37.49.164.202"
        
        "originPortLocal" to call.request.origin.localPort, // 40040
        "originPortServer" to call.request.origin.serverPort, // 50040
        "originPortRemote" to call.request.origin.remotePort, // 52860
        
        "originSchemeProtocol" to call.request.origin.scheme, // "https"
        "originUriPath" to call.request.origin.uri, // "/test/http/proxy-info"
        
        
        
        // get information about the proxy request
        // proxy nginx -> ktor server
        "local" to "proxy nginx -> ktor server",
        
        "localHostLocal" to call.request.local.localHost, // "127.0.0.1"
        "localHostServer" to call.request.local.serverHost, // "localhost"
        "localHostRemote" to call.request.local.remoteHost, // "127.0.0.1"
        
        "localPortLocal" to call.request.local.localPort, // 40040
        "localPortServer" to call.request.local.serverPort, // 40040
        "localPortRemote" to call.request.local.remotePort, // 52860
      )
      
      println("Test HTTP proxy info: $info")
      
      call.respond(info)
    }
    
  }
  
}