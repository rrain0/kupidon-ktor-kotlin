package com.rrain.kupidon.route.check

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserM
import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



suspend inline fun checkFromUserExists(
  call: ApplicationCall,
  fromUserId: UUID,
  onReturn: () -> Unit
): UserM {
  val foundFromUser = collUsers
    .find(Filters.eq(UserM::id.name, fromUserId))
    .firstOrNull()
  
  foundFromUser ?: run {
    call.respondNotFound("NO_FROM_USER", "")
    onReturn()
    throw IllegalStateException()
  }
  
  return foundFromUser
}