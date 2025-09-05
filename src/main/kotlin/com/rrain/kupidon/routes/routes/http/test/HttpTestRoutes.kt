package com.rrain.kupidon.routes.routes.http.test

import com.rrain.utils.ktor.call.printHeaders
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*




// https://ktor.io/docs/server-routing.html#match_url

//  /user/{login} - url with 'login' path parameter segment
//  /user/* - url with any path segment
//  /user/{...} - url with any rest path
//  /user/{param...} - url with 'param' rest path
//  Regex("""/.+/hello""") - we can match url against regex

fun Application.addHttpTestRoutes() {
  routing {
    
    
    //  Example: /test/url/url/*/8
    //  Result: {
    //    "url": "/test/url/url/*/8"
    //  }
    get("/test/url/url/{...}") {
      val url = call.request.uri
      call.respond(mapOf("url" to url))
    }
    
    
    // Example: /test/url/path-params/some-type/any/some-id/888/aa8
    // Result: {
    //   "type": "some-type",
    //   "id": "some-id",
    //   "restAt0": "888",
    //   "rest": ["888", "aa8"],
    // }
    get("/test/url/path-params/{type}/*/{id}/{rest...}") {
      val type = call.request.pathVariables["type"]
      val id = call.request.pathVariables["id"]
      // get only first occurrence
      val restAt0 = call.request.pathVariables["rest"]
      // get all occurrences as list
      val rest = call.request.pathVariables.getAll("rest")
      call.respond(mapOf(
        "type" to type,
        "id" to id,
        "restAt0" to restAt0,
        "rest" to rest,
      ))
    }
    
    
    // Example: /test/url/regex-path-params/aaa/bbbccc/end
    // Result: {
    //   "part": "aaa/bbbc"
    // }
    get(Regex("""/test/url/regex-path-params/(?<part>.*)cc/end""")) {
      val part = call.request.pathVariables["part"]
      call.respond(mapOf(
        "part" to part,
      ))
    }
    
    
    // Example: /test/url/query-params?boolParam&param=s,j&param=h&arrayParam=firs,t&arrayParam=second
    // Result: {
    //   "boolParam": "",
    //   "param": "s,j",
    //   "arrayParam": ["firs,t", "second"]
    // }
    get("/test/url/query-params") {
      // ?boolParam => "", ?boolParam= => ""
      val boolParam = call.request.queryParameters["boolParam"]
      // get only first occurrence
      val param = call.request.queryParameters["param"]
      // get all occurrences as list
      val arrayParam = call.request.queryParameters.getAll("arrayParam")
      call.respond(mapOf(
        "boolParam" to boolParam,
        "param" to param,
        "arrayParam" to arrayParam,
      ))
    }
    
    
    get("/test/http/headers") {
      val headers = call.printHeaders()
      call.respond(headers)
      
      // Set headers: https://ktor.io/docs/responses.html#file
      //call.response.header(HttpHeaders.ContentType, "application/json")
    }
    
    
    get("/test/http/proxy-info") {
      // /manifest.json?lang=ru-RU
      val url = call.request.uri
      
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