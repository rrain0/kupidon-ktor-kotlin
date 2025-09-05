package com.rrain.kupidon.models.db

import kotlinx.datetime.Instant
import java.util.UUID





data class UserToUserLikeM(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  var fromUserId: UUID,
  var toUserId: UUID,
  
  // "2023-06-04T15:21:18.094Z" in string
  var createdAt: Instant,
) {
  
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "fromUserId" to fromUserId,
      "toUserId" to toUserId,
      "createdAt" to createdAt,
    )
  }
  
}
