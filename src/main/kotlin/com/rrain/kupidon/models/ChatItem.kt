package com.rrain.kupidon.models

import com.rrain.kupidon.models.db.ChatMessageM
import kotlinx.datetime.Instant
import java.util.UUID



data class ChatItem(
  var id: UUID,
  var type: ChatType,
  var memberIds: List<UUID>,
  var createdAt: Instant,
  var updatedAt: Instant,
  var profile: ChatProfile? = null,
  var lastMessage: ChatMessageM? = null,
) {
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "type" to type,
      "memberIds" to memberIds,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
      "profile" to profile?.toApi(),
      "lastMessage" to lastMessage?.toApi(),
    )
  }
}