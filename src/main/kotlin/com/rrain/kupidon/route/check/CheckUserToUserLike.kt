package com.rrain.kupidon.route.check

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.model.UserToUserLikeM
import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



fun filterUserToUserLike(fromUserId: UUID, toUserId: UUID) = Filters.and(
  Filters.eq(UserToUserLikeM::fromUserId.name, fromUserId),
  Filters.eq(UserToUserLikeM::toUserId.name, toUserId),
)


suspend inline fun checkUserToUserLikeNotExists(
  call: ApplicationCall,
  fromUserId: UUID,
  toUserId: UUID,
  onReturn: () -> Unit
) {
  val foundLike = collUserToUserLikes
    .find(filterUserToUserLike(fromUserId, toUserId))
    .firstOrNull()
  
  if (foundLike != null) {
    call.respondBadRequest("LIKE_ALREADY_EXISTS", "")
    onReturn()
  }
}


suspend inline fun checkUserToUserLikeExists(
  call: ApplicationCall,
  fromUserId: UUID,
  toUserId: UUID,
  onReturn: () -> Unit
): UserToUserLikeM {
  val foundLike = collUserToUserLikes
    .find(filterUserToUserLike(fromUserId, toUserId))
    .firstOrNull()
  
  foundLike ?: run {
    call.respondNotFound(
      "NO_MUTUAL_LIKE", "You have no chat with this user and have no mutual like"
    )
    onReturn()
    throw IllegalStateException()
  }
  
  return foundLike
}