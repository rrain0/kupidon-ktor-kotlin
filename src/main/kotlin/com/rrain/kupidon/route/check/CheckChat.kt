package com.rrain.kupidon.route.check

import com.mongodb.client.model.Filters
import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.model.db.ChatM
import io.ktor.server.application.ApplicationCall
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


suspend inline fun checkPersonalChatExists(
  call: ApplicationCall,
  memberIds: List<UUID>,
  onReturn: () -> Unit
): ChatM {
  val foundChat = collChats
    .find(filterPersonalChats(memberIds))
    .firstOrNull()
  
  foundChat ?: run {
    call.respondBadRequest(
      "NO_CHAT", "No personal chat between users"
    )
    onReturn()
    throw IllegalStateException()
  }
  
  return foundChat
}