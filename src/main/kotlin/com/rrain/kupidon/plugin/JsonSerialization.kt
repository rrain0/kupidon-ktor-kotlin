package com.rrain.kupidon.plugin

//import io.ktor.serialization.kotlinx.json.*
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
//import com.google.gson.*
import com.rrain.kupidon.util.localDateFormat
import com.rrain.kupidon.util.toLocalDate
import com.rrain.kupidon.util.toZonedDateTime
import com.rrain.kupidon.util.zonedDateTimeFormatter
//import io.ktor.serialization.gson.*
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZonedDateTime


// plugin to serialize response objects as json
// and to deserialize request json to objects
fun Application.configureJsonSerialization() {
  
  install(ContentNegotiation) {
    
    // install Jackson serialization
    jackson {
      configure(SerializationFeature.INDENT_OUTPUT, true)
      /*setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
        indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
        indentObjectsWith(DefaultIndenter("  ", "\n"))
      })*/
      
      configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      
      // support java.time.* types
      registerModule(JavaTimeModule())
      
      registerModule(SimpleModule()
        // ZonedDateTime
        .addSerializer(
          ZonedDateTime::class, object : StdSerializer<ZonedDateTime>(ZonedDateTime::class.java) {
            override fun serialize(value: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider) {
              return gen.writeString(value.format(zonedDateTimeFormatter))
            }
          }
        )
        .addDeserializer(
          ZonedDateTime::class, object : StdDeserializer<ZonedDateTime>(ZonedDateTime::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
              return p.valueAsString.toZonedDateTime()
            }
          }
        )
        
        // LocalDate
        .addSerializer(
          LocalDate::class, object : StdSerializer<LocalDate>(LocalDate::class.java) {
            override fun serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
              return gen.writeString(value.format(localDateFormat))
            }
          }
        )
        .addDeserializer(
          LocalDate::class, object : StdDeserializer<LocalDate>(LocalDate::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
              return p.valueAsString.toLocalDate()
            }
          }
        )
        
        // ObjectId
        .addSerializer(
          ObjectId::class, object : StdSerializer<ObjectId>(ObjectId::class.java) {
            override fun serialize(value: ObjectId, gen: JsonGenerator, provider: SerializerProvider) {
              return gen.writeString(value.toHexString())
            }
          }
        )
        .addDeserializer(
          ObjectId::class, object : StdDeserializer<ObjectId>(ObjectId::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ObjectId {
              return p.valueAsString.let(::ObjectId)
            }
          }
        )
      )
      
      // support Kotlin
      registerModule(KotlinModule.Builder()
        .configure(KotlinFeature.StrictNullChecks, true)
        .build()
      )
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
