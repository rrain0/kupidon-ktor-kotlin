package com.rrain.kupidon.plugins

//import io.ktor.serialization.kotlinx.json.*
import com.google.gson.*
import com.rrain.kupidon.util.localDateFormat
import com.rrain.kupidon.util.timestamptzFormat
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.ZonedDateTime


// plugin to serialize response objects as json
// and to deserialize request json to objects
fun Application.configureJsonSerialization() {
  
  install(ContentNegotiation) {
    // install kotlinx-serialization
    //json()
    
    // install gson serialization
    gson() {
      
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
            return JsonPrimitive(src.format(timestamptzFormat))
          }
          override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
          ): ZonedDateTime {
            return ZonedDateTime.parse(json.asString, timestamptzFormat)
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
      
    }
    
  }
  
}
