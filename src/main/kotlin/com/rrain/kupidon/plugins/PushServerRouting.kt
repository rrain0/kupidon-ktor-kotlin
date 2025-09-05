package com.rrain.kupidon.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.routing
import io.ktor.server.sse.*
import io.ktor.sse.ServerSentEvent
import kotlin.time.Duration.Companion.milliseconds




fun Application.configurePushServerRouting() {
  install(SSE)
  
  
  routing {
    sse("/push-events", serialize = { typeInfo, value ->
      JacksonObjectMapper.writeValueAsString(value)
    }) {
      
      heartbeat {
        period = 15.milliseconds
        event = ServerSentEvent(event = "HEARTBEAT")
      }
      
      // send(TypedServerSentEvent(
      //   event = "HELLO",
      //   data = "Hello from push server!",
      // ))
      send(ServerSentEvent(
        event = "HELLO",
        data = "Hello from push server!",
      ))
    }
  }
}