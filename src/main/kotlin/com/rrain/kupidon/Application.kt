package com.rrain.kupidon

import io.ktor.server.application.*
import com.rrain.kupidon.plugins.*
import com.rrain.kupidon.plugins.configureJsonSerialization
import com.rrain.kupidon.routes.*
import com.rrain.kupidon.routes.util.configureExceptionHandling
import com.rrain.kupidon.routes.test.configureAuthorizationTestRoutes
import com.rrain.kupidon.routes.test.configureHttpTestRoutes
import com.rrain.kupidon.routes.test.configureJsonSerializationTestRoutes
import com.rrain.kupidon.routes.test.configureSendEmailTestRoutes
import com.rrain.kupidon.service.db.configureDatabaseService
import com.rrain.kupidon.service.configureEmailService
import com.rrain.kupidon.service.configureJwtService
import com.rrain.kupidon.service.configurePwdHashing
import com.rrain.kupidon.service.db.init.initDatabase


fun main(args: Array<String>): Unit =
  io.ktor.server.jetty.EngineMain.main(args)


@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
  
  configureLoggingAndMonitoring()
  
  configureJwtService()
  configurePwdHashing()
  configureEmailService()
  
  configureDatabaseService()
  initDatabase()
  
  configureAdministration()
  configureSockets()
  configureJsonSerialization()
  configureHTTP()
  configureJwtAuthentication()
  configureRoleBasedAuthorization()
  
  configureRouting()
  configureExceptionHandling()
  configureJsonSerializationTestRoutes()
  configureHttpTestRoutes()
  configureAuthorizationTestRoutes()
  configureSendEmailTestRoutes()
  configureMainRoutes()
  configurePwaManifestRoute()
  configureAuthRoutes()
  configureRoleRoutes()
  configureUserRoutes()
  
}
