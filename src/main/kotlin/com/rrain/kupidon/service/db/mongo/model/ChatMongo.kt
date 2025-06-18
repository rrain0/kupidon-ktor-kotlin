package com.rrain.kupidon.service.db.mongo.model

import java.time.ZonedDateTime
import java.util.UUID




// TODO Create collection userToChatPrefs

data class ChatMongo(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val id: UUID,
  val participantsIds: List<UUID>,
  
  // "2023-06-04T15:21:18.094Z" in string
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime,
) {
  
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "participantsIds" to participantsIds,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
    )
  }
  
}
