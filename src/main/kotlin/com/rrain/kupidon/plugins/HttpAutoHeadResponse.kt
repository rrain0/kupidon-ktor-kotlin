package com.rrain.kupidon.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*


fun Application.configureHttpAutoHeadResponse() {
  
  /*
    The AutoHeadResponse plugin provides us with the ability to automatically respond
    to a HEAD request for every route that has a GET defined.
    
    It's important to note that if we're using this plugin,
    custom HEAD definitions for the same GET route will be ignored.
   */
  install(AutoHeadResponse)

}


