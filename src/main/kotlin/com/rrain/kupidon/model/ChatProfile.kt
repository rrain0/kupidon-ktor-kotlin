package com.rrain.kupidon.model

import java.util.UUID



data class ChatProfile(
  var id: UUID,
  //val type: ChatProfileType = ChatProfileType.USER,
  var name: String,
  var ava: String,
  // val online
  // val lastOnlineAt
) {
  fun toApi() = mutableMapOf<String, Any?>(
    "id" to id,
    "name" to name,
    "ava" to ava,
  )
}
