package com.rrain.kupidon.route.routes.test

import io.ktor.server.application.*


fun Application.configureTestRoutes() {
  configureStaticRoutes()
  configureJsonSerializationTestRoutes()
  configureHttpTestRoutes()
  configureImgTestRoutes()
  configureAuthorizationTestRoutes()
  configureSendEmailTestRoutes()
  configureMongoTestRoutes()
}