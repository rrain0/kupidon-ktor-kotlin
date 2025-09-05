package com.rrain.kupidon.models

import com.rrain.kupidon.services.live.UserLiveStatusService
import kotlinx.datetime.Instant
import java.util.UUID



data class ChatProfile(
  var id: UUID,
  var type: ChatProfileType,
  var name: String,
  var ava: String,
  var onlineAt: Instant? = null,
  var online: Boolean = false,
) {
  fun toApi() = mutableMapOf<String, Any?>(
    "id" to id,
    "name" to name,
    "ava" to ava,
    
    //"onlineAt" to
    "online" to when (type) {
      ChatProfileType.USER -> UserLiveStatusService.isUserOnline(id)
    },
  )
}
