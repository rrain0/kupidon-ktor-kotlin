package com.rrain.kupidon.plugin

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds



// todo explore websocket
fun Application.configureWebSockets() {
  
  
  install(WebSockets) {
    pingPeriod = 15.seconds
    timeout = 15.seconds
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }
  
  
  routing {
    webSocket("/ws") { // websocket session
      for (frame in incoming) {
        if (frame is Frame.Text) {
          val text = frame.readText()
          outgoing.send(Frame.Text("YOU SAID: $text"))
          if (text.equals("bye", ignoreCase = true)) {
            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
          }
        }
      }
    }
  }
  
  
}
