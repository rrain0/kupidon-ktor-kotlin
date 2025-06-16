package com.rrain.kupidon.service.db.mongo.model

import java.time.ZonedDateTime
import java.util.UUID




data class ChatMessageContent(
  val text: String,
) {
  fun convertToSend(): MutableMap<String, Any?> {
    return mutableMapOf(
      "text" to text,
    )
  }
}


data class ChatMessageMongo(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val id: UUID,
  
  // "2023-06-04T15:21:18.094Z" in string
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime,
  
  val content: ChatMessageContent,
) {
  
  fun convertToSend(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
      "content" to content.convertToSend(),
    )
  }
  
}
