package com.rrain.kupidon.routes.`check-data`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.models.ChatType
import com.rrain.kupidon.routes.`response-errors`.respondBadRequest
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.services.mongo.collChats
import com.rrain.kupidon.models.db.ChatM
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.RoutingContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



suspend inline fun checkChatExists(
  call: ApplicationCall,
  chatId: UUID,
  userId: UUID,
  onReturn: () -> Unit
): ChatM {
  val foundChat = collChats
    .find(Filters.and(
      Filters.eq(ChatM::id.name, chatId),
      Filters.all(ChatM::memberIds.name, userId),
    ))
    .firstOrNull()
  
  foundChat ?: run {
    call.respondNotFound(
      "NO_CHAT", "No chat or user is not member of chat"
    )
    onReturn()
    throw IllegalStateException()
  }
  
  return foundChat
}



fun filterPersonalChats(memberIds: List<UUID>) = Filters.and(
  Filters.eq(ChatM::type.name, ChatType.PERSONAL),
  Filters.eq(ChatM::memberIds.name, memberIds),
)


context(routingContext: RoutingContext)
suspend inline fun checkPersonalChatExists(
  memberIds: List<UUID>,
): ChatM {
  val foundChat = collChats
    .find(filterPersonalChats(memberIds))
    .firstOrNull()
  
  foundChat ?: run {
    routingContext.call.respondBadRequest(
      "NO_CHAT", "No personal chat between users"
    )
    throw IllegalStateException()
  }
  
  return foundChat
}