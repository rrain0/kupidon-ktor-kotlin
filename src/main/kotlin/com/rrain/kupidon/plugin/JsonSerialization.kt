package com.rrain.kupidon.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.rrain.util.base.json.configureJson
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*



lateinit var JacksonObjectMapper: ObjectMapper

// Plugin to serialize response body object to json
// and to deserialize request body from json to objects
fun Application.configureJsonSerialization() {
  
  
  
  install(ContentNegotiation) {
    
    // Install Jackson serialization
    jackson {
      JacksonObjectMapper = this.configureJson()
    }
    
  }
  
  
  
}
