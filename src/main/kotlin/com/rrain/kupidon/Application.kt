package com.rrain.kupidon

import io.ktor.server.application.*
import com.rrain.kupidon.plugin.*
import com.rrain.kupidon.plugin.configureJsonSerialization
import com.rrain.kupidon.route.*
import com.rrain.kupidon.plugin.configureStatusPages
import com.rrain.kupidon.service.configureEmailService
import com.rrain.kupidon.service.configureJwtService
import com.rrain.kupidon.service.configurePwdHashing
import com.rrain.kupidon.service.mongo.configureMongoDbService


fun main(args: Array<String>) {
  io.ktor.server.jetty.jakarta.EngineMain.main(args)
}



// application.conf references this function.
fun Application.module() {
  configureLogging()
  configureJsonSerialization()
  
  configureJwtService()
  configurePwdHashing()
  configureEmailService()
  
  configureMongoDbService()
  
  configureHttpCachingHeaders()
  configureHttpForwardedHeaders()
  configureHttpAutoHeadResponse()
  
  configureJwtAuthentication()
  configureStatusPages()
  configureWebSockets()
  configureRouting()
}
