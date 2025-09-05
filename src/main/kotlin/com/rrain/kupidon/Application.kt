package com.rrain.kupidon

import io.ktor.server.application.*
import com.rrain.kupidon.plugins.*
import com.rrain.kupidon.plugins.configureJsonSerialization
import com.rrain.kupidon.plugins.configureStatusPages
import com.rrain.kupidon.plugins.configureWebSocketRouting
import com.rrain.kupidon.services.email.configureEmailService
import com.rrain.kupidon.services.jwt.configureJwtService
import com.rrain.kupidon.services.`pwd-hash`.configurePwdHashService
import com.rrain.kupidon.services.mongo.configureMongoDbService



fun main(args: Array<String>) {
  io.ktor.server.jetty.jakarta.EngineMain.main(args)
}



// application.conf references this function.
fun Application.module() {
  configureLogging()
  configureJsonSerialization()
  
  configureJwtService()
  configurePwdHashService()
  configureEmailService()
  
  configureMongoDbService()
  
  configureHttpCachingHeaders()
  configureHttpForwardedHeaders()
  configureHttpAutoHeadResponse()
  
  configureJwtAuthentication()
  configureStatusPages()
  configureRouting()
  configureHttpRouting()
  configurePushServerRouting()
  configureWebSocketRouting()
}
