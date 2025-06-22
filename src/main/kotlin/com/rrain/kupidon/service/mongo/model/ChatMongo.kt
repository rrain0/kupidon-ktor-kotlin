package com.rrain.kupidon.service.mongo.model

import com.rrain.kupidon.model.ChatType
import kotlinx.datetime.Instant
import java.util.UUID




// TODO Create collection userToChatPrefs

data class ChatMongo(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  var id: UUID,
  var type: ChatType,
  var participantsIds: List<UUID>,
  
  // "2023-06-04T15:21:18.094Z" in string
  var createdAt: Instant,
  var updatedAt: Instant,
) {
  
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "type" to type,
      "participantsIds" to participantsIds,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
    )
  }
  
}
