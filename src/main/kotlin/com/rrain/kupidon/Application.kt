package com.rrain.kupidon

import io.ktor.server.application.*
import com.rrain.kupidon.plugin.*
import com.rrain.kupidon.plugin.configureJsonSerialization
import com.rrain.kupidon.route.*
import com.rrain.kupidon.route.util.configureExceptionHandling
import com.rrain.kupidon.service.configureEmailService
import com.rrain.kupidon.service.configureJwtService
import com.rrain.kupidon.service.configurePwdHashing
import com.rrain.kupidon.service.db.mongo.configureMongoDbService



fun main(args: Array<String>): Unit =
  io.ktor.server.jetty.EngineMain.main(args)



// application.conf references the main function.
// This annotation prevents the IDE from marking it as unused.
@Suppress("unused")
fun Application.module() {
  
  configureLoggingAndMonitoring()
  
  configureJwtService()
  configurePwdHashing()
  configureEmailService()
  
  configureMongoDbService()
  
  configureAdministration()
  configureSockets()
  configureJsonSerialization()
  configureHTTP()
  configureJwtAuthentication()
  configureRoleBasedAuthorization()
  
  configureExceptionHandling()
  configureRouting()
  
}
