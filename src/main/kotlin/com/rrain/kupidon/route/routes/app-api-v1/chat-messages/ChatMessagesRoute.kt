package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-messages`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatsMessages
import com.rrain.kupidon.service.mongo.model.ChatMessageMongo
import com.rrain.kupidon.service.mongo.model.ChatMongo
import com.rrain.kupidon.service.mongo.useSingleDocTx
import com.rrain.util.uuid.toUuid
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList



// TODO put sort into db flow
// TODO put mapToApi into flow

fun Application.addChatMessagesRoute() {
  routing {
    authenticate {
      get(ApiV1Routes.chatMessages) {
        val userUuid = authUserUuid
        
        val toUserUuid = call.parameters["toUserId"]?.toUuid()
        if (toUserUuid == null) {
          return@get call.respondInvalidParams("'toUserId' search param must be uuid-string")
        }
        
        
        
        val chats = collChats
        val chatsMessages = collChatsMessages
        
        val messages = useSingleDocTx { session ->
          val nParticipantsIds = ChatMongo::participantsIds.name
          
          val nMessageChatId = ChatMessageMongo::chatId.name
          
          val participantsIds = listOf(userUuid, toUserUuid)
          
          val chat = chats
            .find(
              Filters.and(
                Filters.all(nParticipantsIds, participantsIds),
                Filters.size(nParticipantsIds, 2)
              )
            )
            .firstOrNull()
          
          if (chat == null) {
            return@get call.respondInvalidBody("NO_CHAT")
          }
          
          val messages = chatsMessages
            .find(Filters.eq(nMessageChatId, chat.id))
            .toList()
            .sortedBy { it.createdAt }
          
          messages
        }
        
        call.respond(mapOf(
          "messages" to messages.map { it.toApi() },
        ))
      }
    }
  }
}