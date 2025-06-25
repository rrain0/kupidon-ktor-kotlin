package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-messages`

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatMessages
import com.rrain.kupidon.service.mongo.model.ChatMessageM
import com.rrain.kupidon.service.mongo.model.ChatM
import com.rrain.`util-ktor`.call.queryParams
import com.rrain.util.uuid.toUuid
import com.rrain.util.uuid.uuidComparator
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList



fun Application.addRouteGetChatMessages() {
  routing {
    authenticate {
      get(ApiV1Routes.chatMessages) {
        val userUuid = authUserUuid
        
        val toUserUuid = call.queryParams["toUserId"]?.toUuid()
        val toChatUuid = call.queryParams["toChatId"]?.toUuid()
        
        val chatId = run {
          if (toChatUuid != null) {
            val chat = collChats
              .find(Filters.and(
                Filters.eq(ChatM::id.name, toChatUuid),
                Filters.all(ChatM::memberIds.name, userUuid),
              ))
              .firstOrNull()
            
            chat ?: return@get call.respondBadRequest(
              "NO_CHAT",
              "No chat with id $toChatUuid or user with id $userUuid is not member of this chat"
            )
            
            toChatUuid
          }
          else if (toUserUuid != null) {
            val memberIds = listOf(userUuid, toUserUuid).sortedWith(uuidComparator)
            
            val chat = collChats
              .find(Filters.all(ChatM::memberIds.name, memberIds))
              .firstOrNull()
            
            chat ?: return@get call.respondBadRequest(
              "NO_CHAT", "No chat between users with ids $memberIds"
            )
            
            chat.id
          }
          else {
            return@get call.respondInvalidParams(
              "'toUserId' or 'toChatId' search param must be uuid-string",
            )
          }
        }
        
        val messagesToApi = collChatMessages
          .find(Filters.eq(ChatMessageM::chatId.name, chatId))
          .sort(Sorts.ascending(ChatMessageM::createdAt.name))
          .map { it.toApi() }
          .toList()
        
        call.respond(mapOf(
          "messages" to messagesToApi,
        ))
      }
    }
  }
}