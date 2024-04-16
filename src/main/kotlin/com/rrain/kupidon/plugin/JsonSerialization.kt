package com.rrain.kupidon.plugin

//import io.ktor.serialization.kotlinx.json.*
//import com.google.gson.*
//import io.ktor.serialization.gson.*
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.Separators
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import com.rrain.kupidon.util.*
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID


lateinit var JacksonObjectMapper: ObjectMapper



// plugin to serialize response objects as json
// and to deserialize request json to objects
fun Application.configureJsonSerialization() {
  
  
  
  install(ContentNegotiation) {
    
    // install Jackson serialization
    jackson {
      JacksonObjectMapper = this
      
      configure(SerializationFeature.INDENT_OUTPUT, true)
      setDefaultPrettyPrinter(DefaultPrettyPrinter(
        // no space before colon and have space after colon
        Separators.createDefaultInstance().withObjectFieldValueSpacing(Separators.Spacing.AFTER)
      ))
      /*setDefaultPrettyPrinter({
        class MyPrettyPrinter : DefaultPrettyPrinter() {
          override fun createInstance(): DefaultPrettyPrinter = MyPrettyPrinter()
          override fun writeObjectFieldValueSeparator(g: JsonGenerator) = g.writeRaw(": ")
        }
        MyPrettyPrinter()
      }())*/
      /*setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
        indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
        indentObjectsWith(DefaultIndenter("  ", "\n"))
      })*/
      
      configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
      
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
        
        // java.util.UUID
        .addSerializer(
          UUID::class, object : StdSerializer<UUID>(UUID::class.java) {
            override fun serialize(value: UUID, gen: JsonGenerator, provider: SerializerProvider) {
              return gen.writeString(value.toString())
            }
          }
        )
        .addDeserializer(
          UUID::class, object : StdDeserializer<UUID>(UUID::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UUID {
              return p.valueAsString.toUuid()
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
