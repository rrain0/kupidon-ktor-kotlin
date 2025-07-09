package com.rrain.kupidon.route.`check-data`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserM
import io.ktor.server.routing.RoutingContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



context(routingContext: RoutingContext)
suspend inline fun checkFromUserExists(
  fromUserId: UUID,
): UserM {
  val foundFromUser = collUsers
    .find(Filters.eq(UserM::id.name, fromUserId))
    .firstOrNull()
  
  foundFromUser ?: run {
    routingContext.call.respondNotFound(
      "NO_FROM_USER", ""
    )
    throw IllegalStateException()
  }
  
  return foundFromUser
}