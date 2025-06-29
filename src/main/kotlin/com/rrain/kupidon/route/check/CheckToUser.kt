package com.rrain.kupidon.route.check

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserM
import io.ktor.server.routing.RoutingContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



context(routingContext: RoutingContext)
suspend inline fun checkToUserExists(
  toUserId: UUID,
): UserM {
  val foundToUser = collUsers
    .find(Filters.eq(UserM::id.name, toUserId))
    .firstOrNull()
  
  foundToUser ?: run {
    routingContext.call.respondNotFound(
      "NO_TO_USER", ""
    )
    throw IllegalStateException()
  }
  
  return foundToUser
}