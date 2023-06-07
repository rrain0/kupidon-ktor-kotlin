package com.rrain.kupidon.plugins

import ch.qos.logback.classic.LoggerContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level


fun Application.configureLoggingAndMonitoring(){
  
  // already configured in resources/logback.xml
  //val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
  //loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.DEBUG
  
  install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/") }
    callIdMdc("call-id")
  }
  
  install(CallId) {
    header(HttpHeaders.XRequestId)
    verify { callId: String ->
      callId.isNotEmpty()
    }
  }
  
}