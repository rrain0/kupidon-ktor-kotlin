package com.rrain.kupidon.models

import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import kotlinx.datetime.Instant
import java.util.UUID



data class UserPair(
  var fromUserId: UUID,
  var toUserId: UUID,
  var createdAt: Instant,
  var toUser: UserM,
) {
  fun toApi(
    host: String,
    port: Int
  ) = mutableMapOf<String, Any?>(
    "fromUserId" to fromUserId,
    "toUserId" to toUserId,
    "createdAt" to createdAt,
    "toUser" to toUser.toApi(UserDataType.StrangerShort, host, port, showStatus = true),
  )
}