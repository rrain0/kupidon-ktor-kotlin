package com.rrain.kupidon.plugins

import com.rrain.utils.base.any.ifNull
import com.rrain.utils.base.any.ifTruly
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level



fun Application.configureLogging() {
  
  /*
  The CallId plugin allows you to trace client requests end-to-end by using unique request IDs or call IDs.
  Typically, working with a call ID in Ktor might look as follows:

  1) First, you need to obtain a call ID for a specific request in one of the following ways:
  
  1.1) A reverse proxy (such as Nginx) or cloud provider (such as Heroku) might add a call ID in a specific header,
    for example, X-Request-Id. In this case, Ktor allows you to retrieve a call ID.
  
  1.2) Otherwise, if a request comes without a call ID, you can generate it on the Ktor server.
  
  2) Next, Ktor verifies a retrieved/generated call ID using a predefined dictionary.
    You can also provide your own condition to verify a call ID.
  
  3) Finally, you can send a call ID to the client in a specific header, for example, X-Request-Id.
  
  Using CallId along with CallLogging helps you troubleshoot calls by putting a call ID in the MDC context
  and configuring a logger to show a call ID for each request.
   */
  
  install(CallId) {
    
    
    // Retrieve from header
    //retrieveFromHeader(HttpHeaders.XRequestId)
    
    // Retrieve from ApplicationCall
    //retrieve { call -> call.request.header(HttpHeaders.XRequestId) }
    
    
    // Generate by function
    /*
      val counter = atomic(0)
      generate {
          "generated-call-id-${counter.getAndIncrement()}"
      }
     */
    
    // Generate by dictionary
    generate(8, "0123456789abcdef")
    
    
    // Send to header
    //replyToHeader(HttpHeaders.XRequestId)
    
    // Send to header using ApplicationCall
    //reply { call, callId -> call.response.header(HttpHeaders.XRequestId, callId) }
    
    // Retrieve from header and send to the same header
    // X-Request-ID: d3433a2ffe
    header(HttpHeaders.XRequestId)
    
    // Defines if need to send call-id to header
    verify { callId: String ->
      callId.isNotEmpty()
    }
  }
  
  
  install(CallLogging) {
    // Define log level for call logging
    level = Level.INFO
    
    // filter calls to log
    filter { call -> call.request.path().startsWith("/") }
    
    // Put callId to MDC (Mapped Diagnostic Context) to "call-id" variable
    // Then you can use "call-id" in logger config for example in logback.xml as "%X{call-id}"
    callIdMdc("call-id")
    
    // You can add arbitrary parameters to MDC
    // For example "name" query param will be put to MDC "name-parameter"
    /*
    mdc("name-parameter") { call ->
      call.request.queryParameters["name"]
    }*/
    
    // It will be "%msg" variable in logback.xml
    format { call ->
      val callId = call.response.headers[HttpHeaders.XRequestId]
      val status = call.response.status()?.value
      val statusText = call.response.status()?.description
      val httpMethod = call.request.httpMethod.value
      val userAgent = call.request.headers["User-Agent"]
      val url = call.request.origin.uri
      
      listOf(
        callId.ifNull { "" }.ifTruly { "call-id=$it" },
        httpMethod,
        status,
        "$statusText:",
        url
      ).joinToString(" ")
    }
  }
  
  
  // Already configured in resources/logback.xml
  //val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
  //loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.DEBUG
  
  
}