package com.rrain.kupidon.route.routes.test

import io.ktor.server.application.*


fun Application.configureTestRoutes() {
  configureStaticRoutes()
  configureJsonSerializationTestRoutes()
  configureHttpTestRoutes()
  configureAuthorizationTestRoutes()
  configureSendEmailTestRoutes()
  configureMongoTestRoutes()
}