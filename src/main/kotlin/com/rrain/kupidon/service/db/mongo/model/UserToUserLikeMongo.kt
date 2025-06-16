package com.rrain.kupidon.service.db.mongo.model

import java.time.ZonedDateTime
import java.util.UUID





data class UserToUserLikeMongo(
  // UUID e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val fromUserId: UUID,
  val toUserId: UUID,
  
  // "2023-06-04T15:21:18.094Z" in string
  val created: ZonedDateTime,
) {
  
  fun convertToSend(): Map<String, Any?> {
    return mutableMapOf(
      "fromUserId" to fromUserId,
      "toUserId" to toUserId,
      "created" to created,
    )
  }
  
}
