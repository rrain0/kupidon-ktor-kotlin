package com.rrain.utils.bson

import com.rrain.kupidon.models.ChatType
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonMode
import org.bson.json.JsonWriter
import org.bson.json.JsonWriterSettings
import java.io.StringWriter
import kotlin.text.get



fun main() {
  
  println(Document("", listOf(1)))
  
  println(Document.parse($$"""
    { $and: [
      { b: $${ChatType.PERSONAL.toBJson()} },
    ] }
    """.trimIndent()))
  
}


val docEmptyKeyToValuePattern = Regex("""[{]"": (?<v>.*)}$""", RegexOption.DOT_MATCHES_ALL)
fun Any?.toBJson(codecRegistry: CodecRegistry = appBsonCodecRegistry): String? {
  val doc = Document("", this)
  return doc.toJson(DocumentCodec(codecRegistry))
    .let { docEmptyKeyToValuePattern.matchEntire(it)!!.groups["v"]!!.value }
}


// Оно не хочет кодировать просто значения
private inline fun <reified T> T.__toBsonJson(codecRegistry: CodecRegistry = appBsonCodecRegistry): String? {
  val writer = JsonWriter(
    StringWriter(),
    JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()
  )
  if (this == null) writer.writeNull()
  else {
    val encoder = codecRegistry.get(T::class.java)
    val encoderContext = EncoderContext.builder().build()
    encoder.encode(writer, this, encoderContext)
  }
  return writer.writer.toString()
}
