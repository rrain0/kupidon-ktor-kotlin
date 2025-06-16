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
  val created: ZonedDateTime,
  val updated: ZonedDateTime,
  
  val content: ChatMessageContent,
) {
  
  fun convertToSend(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "created" to created,
      "updated" to updated,
      "content" to content.convertToSend(),
    )
  }
  
}
