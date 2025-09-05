package com.rrain.kupidon.models.db

import com.rrain.kupidon.models.ChatType
import kotlinx.datetime.Instant
import java.util.UUID




// TODO Create collection userToChatPrefs

data class ChatM(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  var id: UUID,
  var type: ChatType,
  var memberIds: List<UUID>,
  
  // "2023-06-04T15:21:18.094Z" in string
  var createdAt: Instant,
  var updatedAt: Instant,
) {
  
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "type" to type,
      "memberIds" to memberIds,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
    )
  }
  
}
