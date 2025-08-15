package com.rrain.kupidon

import io.ktor.server.application.*
import com.rrain.kupidon.plugin.*
import com.rrain.kupidon.plugin.configureJsonSerialization
import com.rrain.kupidon.plugin.configureStatusPages
import com.rrain.kupidon.plugin.configureWebSocketRouting
import com.rrain.kupidon.service.email.configureEmailService
import com.rrain.kupidon.service.jwt.configureJwtService
import com.rrain.kupidon.service.`pwd-hash`.configurePwdHashService
import com.rrain.kupidon.service.mongo.configureMongoDbService



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
