package com.rrain.kupidon.route.route.test

import com.rrain.kupidon.route.route.ARCHIVE.postgres.test.configurePostgresTestRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureTestRoutes(){
  configureJsonSerializationTestRoutes()
  configureHttpTestRoutes()
  configureAuthorizationTestRoutes()
  configureSendEmailTestRoutes()
  configureMongoTestRoutes()
  configurePostgresTestRoutes()
  
  
  
  
  
  routing {
  
  
    
    
    
  }
  
}