package com.rrain.utils.bson

import com.mongodb.MongoClientSettings
import com.rrain.utils.base.`date-time`.asTimestampToInstant
import com.rrain.utils.base.`date-time`.toLocalDateInUtc
import com.rrain.utils.base.`date-time`.toTimestamp
import com.rrain.utils.base.`date-time`.toUtcInstant
import com.rrain.utils.base.`date-time`.toZonedDateTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.UuidRepresentation
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import java.time.ZonedDateTime




val appBsonCodecRegistry = CodecRegistries.fromRegistries(
  // !!! Earlier declarations have higher priority
  CodecRegistries.fromCodecs(
    object : Codec<Instant> {
      override fun getEncoderClass() = Instant::class.java
      override fun encode(writer: BsonWriter, value: Instant, encoderContext: EncoderContext) {
        writer.writeDateTime(value.toTimestamp())
      }
      override fun decode(reader: BsonReader, decoderContext: DecoderContext): Instant {
        return reader.readDateTime().asTimestampToInstant()
      }
    },
    object : Codec<LocalDate> {
      override fun getEncoderClass() = LocalDate::class.java
      override fun encode(writer: BsonWriter, value: LocalDate, encoderContext: EncoderContext) {
        writer.writeDateTime(value.toUtcInstant().toTimestamp())
      }
      override fun decode(reader: BsonReader, decoderContext: DecoderContext): LocalDate {
        return reader.readDateTime().asTimestampToInstant().toLocalDateInUtc()
      }
    },
    object : Codec<ZonedDateTime> {
      override fun getEncoderClass() = ZonedDateTime::class.java
      override fun encode(writer: BsonWriter, value: ZonedDateTime, encoderContext: EncoderContext) {
        writer.writeDateTime(value.toTimestamp())
      }
      override fun decode(reader: BsonReader, decoderContext: DecoderContext): ZonedDateTime {
        return reader.readDateTime().toZonedDateTime()
      }
    },
  ),
  MongoClientSettings.getDefaultCodecRegistry(),
).let {
  CodecRegistries.withUuidRepresentation(it, UuidRepresentation.STANDARD)
}
