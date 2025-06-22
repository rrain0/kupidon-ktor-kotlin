package com.rrain.kupidon.service.mongo.model

import kotlinx.datetime.Instant
import java.util.UUID




data class ChatMessageContentMongo(
  var text: String,
) {
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "text" to text,
    )
  }
}


data class ChatMessageMongo(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  var id: UUID,
  var chatId: UUID,
  var fromUserId: UUID,
  
  // "2023-06-04T15:21:18.094Z" in string
  var createdAt: Instant,
  var updatedAt: Instant,
  
  var content: ChatMessageContentMongo,
) {
  
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "chatId" to chatId,
      "fromUserId" to fromUserId,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
      "content" to content.toApi(),
    )
  }
  
}
