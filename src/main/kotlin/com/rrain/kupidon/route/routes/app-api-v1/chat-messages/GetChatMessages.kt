package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-messages`

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.check.checkChatExists
import com.rrain.kupidon.route.check.checkPersonalChatExists
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChatMessages
import com.rrain.kupidon.model.db.ChatMessageM
import com.rrain.`util-ktor`.call.queryParams
import com.rrain.util.uuid.toUuid
import com.rrain.util.uuid.uuidComparator
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList



fun Application.addRouteGetChatMessages() {
  routing {
    authenticate {
      get(ApiV1Routes.chatMessages) {
        val userId = authUserUuid
        
        val toUserId = call.queryParams["toUserId"]?.toUuid()
        val toChatId = call.queryParams["toChatId"]?.toUuid()
        
        val chatId = run {
          if (toChatId != null) {
            checkChatExists(call, toChatId, userId) { return@get }
            toChatId
          }
          else if (toUserId != null) {
            val memberIds = listOf(userId, toUserId).sortedWith(uuidComparator)
            val chat = checkPersonalChatExists(call, memberIds) { return@get }
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