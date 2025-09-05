package com.rrain.utils.base.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.util.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import com.rrain.utils.base.`date-time`.formatToString
import com.rrain.utils.base.`date-time`.localDateFormat
import com.rrain.utils.base.`date-time`.toInstant
import com.rrain.utils.base.`date-time`.toJavaLocalDate
import com.rrain.utils.base.`date-time`.toLocalDate
import com.rrain.utils.base.`date-time`.toZonedDateTime
import com.rrain.utils.base.`date-time`.zonedDateTimeFormatter
import com.rrain.utils.base.uuid.toUuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.time.ZonedDateTime
import java.util.*



fun createJacksonJsonMapper(): ObjectMapper {
  return JsonMapper().configureJson()
}


fun ObjectMapper.configureJson(): ObjectMapper {
  this.configureJsonTypes()
  this.configureJsonPrettier()
  return this
}


fun ObjectMapper.configureJsonPrettier(): ObjectMapper {
  configure(SerializationFeature.INDENT_OUTPUT, true)
  
  setDefaultPrettyPrinter(
    DefaultPrettyPrinter(
      // no space before colon and have space after colon
      Separators.createDefaultInstance().withObjectFieldValueSpacing(Separators.Spacing.AFTER)
    ).apply {
      indentObjectsWith(DefaultIndenter(
        "  ", // add this before start of each property in object
        "\n" // add this after property in object
      ))
      indentArraysWith(DefaultIndenter(
        "  ", // add this before start of each property in array
        "\n" // add this after property in array
      ))
    }
  )
  /*
  setDefaultPrettyPrinter({
    class MyPrettyPrinter : DefaultPrettyPrinter() {
      override fun createInstance(): DefaultPrettyPrinter = MyPrettyPrinter()
      override fun writeObjectFieldValueSeparator(g: JsonGenerator) = g.writeRaw(": ")
    }
    MyPrettyPrinter()
  }())
  */
  /*
  setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
    indentObjectsWith(DefaultIndenter("  ", "\n"))
  })
  */
  return this
}


fun ObjectMapper.configureJsonTypes(): ObjectMapper {
  
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
  
  // support java.time.* types
  registerModule(JavaTimeModule())
  
  registerModule(
    SimpleModule()
      // kotlinx.datetime.Instant
      .addSerializer(
        Instant::class,
        object : StdSerializer<Instant>(Instant::class.java) {
          override fun serialize(value: Instant, gen: JsonGenerator, provider: SerializerProvider) {
            return gen.writeString(value.formatToString())
          }
        }
      )
      .addDeserializer(
        Instant::class,
        object : StdDeserializer<Instant>(Instant::class.java) {
          override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
            return p.valueAsString.toInstant()
          }
        }
      )
      
      // kotlinx.datetime.LocalDate
      .addSerializer(
        LocalDate::class,
        object : StdSerializer<LocalDate>(LocalDate::class.java) {
          override fun serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
            return gen.writeString(value.formatToString())
          }
        }
      )
      .addDeserializer(
        LocalDate::class,
        object : StdDeserializer<LocalDate>(LocalDate::class.java) {
          override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
            return p.valueAsString.toLocalDate()
          }
        }
      )
      
      // java.time.ZonedDateTime
      .addSerializer(
        ZonedDateTime::class,
        object : StdSerializer<ZonedDateTime>(ZonedDateTime::class.java) {
          override fun serialize(value: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider) {
            return gen.writeString(value.format(zonedDateTimeFormatter))
          }
        }
      )
      .addDeserializer(
        ZonedDateTime::class,
        object : StdDeserializer<ZonedDateTime>(ZonedDateTime::class.java) {
          override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
            return p.valueAsString.toZonedDateTime()
          }
        }
      )
      
      // java.time.LocalDate
      .addSerializer(
        java.time.LocalDate::class,
        object : StdSerializer<java.time.LocalDate>(java.time.LocalDate::class.java) {
          override fun serialize(value: java.time.LocalDate, gen: JsonGenerator, provider: SerializerProvider) {
            return gen.writeString(value.format(localDateFormat))
          }
        }
      )
      .addDeserializer(
        java.time.LocalDate::class,
        object : StdDeserializer<java.time.LocalDate>(java.time.LocalDate::class.java) {
          override fun deserialize(p: JsonParser, ctxt: DeserializationContext): java.time.LocalDate {
            return p.valueAsString.toJavaLocalDate()
          }
        }
      )
      
      // java.util.UUID
      .addSerializer(
        UUID::class,
        object : StdSerializer<UUID>(UUID::class.java) {
          override fun serialize(value: UUID, gen: JsonGenerator, provider: SerializerProvider) {
            return gen.writeString(value.toString())
          }
        }
      )
      .addDeserializer(
        UUID::class,
        object : StdDeserializer<UUID>(UUID::class.java) {
          override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UUID {
            return p.valueAsString.toUuid()
          }
        }
      )
    
    // ObjectId for MongoDB
    /*.addSerializer(
      ObjectId::class,
      object : StdSerializer<ObjectId>(ObjectId::class.java) {
        override fun serialize(value: ObjectId, gen: JsonGenerator, provider: SerializerProvider) {
          return gen.writeString(value.toHexString())
        }
      }
    )
    .addDeserializer(
      ObjectId::class,
      object : StdDeserializer<ObjectId>(ObjectId::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ObjectId {
          return p.valueAsString.let(::ObjectId)
        }
      }
    )*/
  
  )
  
  // support Kotlin
  registerModule(
    KotlinModule.Builder()
      .configure(KotlinFeature.StrictNullChecks, true)
      .build()
  )
  
  return this
}

