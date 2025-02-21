package com.rrain.kupidon.route.routes.test

import io.ktor.server.application.*


fun Application.configureTestRoutes() {
  configureJsonSerializationTestRoutes()
  configureHttpTestRoutes()
  configureImgTestRoutes()
  configureAuthorizationTestRoutes()
  configureSendEmailTestRoutes()
  configureMongoTestRoutes()
}