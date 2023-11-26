package com.rrain.kupidon.route.route.test

import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureTestRoutes(){
  typeSafeRoutingTest()
  configureStaticRoutes()
  configureJsonSerializationTestRoutes()
  configureHttpTestRoutes()
  configureAuthorizationTestRoutes()
  configureSendEmailTestRoutes()
  configureMongoTestRoutes()
  
  
  
  
  
  routing {
  
  
    
  }
  
  
  
}