package com.rrain.kupidon.plugin

//import io.ktor.serialization.kotlinx.json.*
//import com.google.gson.*
//import io.ktor.serialization.gson.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.rrain.kupidon.service.json.configureJson
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*


lateinit var JacksonObjectMapper: ObjectMapper



// plugin to serialize response objects as json
// and to deserialize request json to objects
fun Application.configureJsonSerialization() {
  
  
  
  install(ContentNegotiation) {
    
    // install Jackson serialization
    jackson {
      JacksonObjectMapper = this.configureJson()
    }
    
    
    // install kotlinx-serialization
    //json()
    
    // install gson serialization
    /*gson() {
      
      serializeNulls()
      setPrettyPrinting()
      
      registerTypeAdapter(
        ZonedDateTime::class.java,
        object : JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
          override fun serialize(
            src: ZonedDateTime,
            typeOfSrc: Type,
            context: JsonSerializationContext
          ): JsonElement {
            return JsonPrimitive(src.format(zonedDateTimeFormat))
          }
          override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
          ): ZonedDateTime {
            return ZonedDateTime.parse(json.asString, zonedDateTimeFormat)
          }
        }
      )
      
      registerTypeAdapter(
        LocalDate::class.java,
        object : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
          override fun serialize(
            src: LocalDate,
            typeOfSrc: Type,
            context: JsonSerializationContext
          ): JsonElement {
            return JsonPrimitive(src.format(localDateFormat))
          }
          override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
          ): LocalDate {
            return LocalDate.parse(json.asString, localDateFormat)
          }
        }
      )
      
    }*/
    
    
  }
  
  
  
}
