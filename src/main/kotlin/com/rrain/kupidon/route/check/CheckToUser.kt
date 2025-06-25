package com.rrain.kupidon.route.check

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserM
import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



suspend inline fun checkToUserExists(
  call: ApplicationCall,
  toUserId: UUID,
  onReturn: () -> Unit
): UserM {
  val foundToUser = collUsers
    .find(Filters.eq(UserM::id.name, toUserId))
    .firstOrNull()
  
  foundToUser ?: run {
    call.respondNotFound("NO_TO_USER", "")
    onReturn()
    throw IllegalStateException()
  }
  
  return foundToUser
}