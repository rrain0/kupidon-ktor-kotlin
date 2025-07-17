package com.rrain.kupidon.route.`check-data`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.model.db.UserToUserLikeM
import io.ktor.server.routing.RoutingContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



fun filterUserToUserLike(fromUserId: UUID, toUserId: UUID) = Filters.and(
  Filters.eq(UserToUserLikeM::fromUserId.name, fromUserId),
  Filters.eq(UserToUserLikeM::toUserId.name, toUserId),
)


context(routingContext: RoutingContext)
suspend inline fun checkUserToUserLikeNotExists(
  fromUserId: UUID,
  toUserId: UUID,
) {
  val foundLike = collUserToUserLikes
    .find(filterUserToUserLike(fromUserId, toUserId))
    .firstOrNull()
  
  if (foundLike != null) {
    routingContext.call.respondBadRequest(
      "LIKE_ALREADY_EXISTS", ""
    )
  }
}



context(routingContext: RoutingContext)
suspend inline fun checkUsersPairExists(
  fromUserId: UUID,
  toUserId: UUID,
) {
  val toLike = collUserToUserLikes
    .find(filterUserToUserLike(fromUserId, toUserId))
    .firstOrNull()
  
  toLike ?: run {
    routingContext.call.respondNotFound(
      "NO_USERS_PAIR", "You have no pair with this user"
    )
    throw IllegalStateException()
  }
  
  val fromLike = collUserToUserLikes
    .find(filterUserToUserLike(toUserId, fromUserId))
    .firstOrNull()
  
  fromLike ?: run {
    routingContext.call.respondNotFound(
      "NO_USERS_PAIR", "You have no pair with this user"
    )
    throw IllegalStateException()
  }
}