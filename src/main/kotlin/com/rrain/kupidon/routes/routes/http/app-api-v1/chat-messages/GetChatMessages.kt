package com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-messages`

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`check-data`.checkChatExists
import com.rrain.kupidon.routes.`check-data`.checkPersonalChatExists
import com.rrain.kupidon.routes.`response-errors`.respondInvalidParams
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.collChatMessages
import com.rrain.kupidon.models.db.ChatMessageM
import com.rrain.kupidon.routes.`convert-or-error`.toUuidOr400
import com.rrain.utils.ktor.call.queryParams
import com.rrain.utils.base.uuid.uuidAsStringComparator
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
        val userId = authUserId
        
        val toUserId = call.queryParams["toUserId"]?.toUuidOr400()
        val toChatId = call.queryParams["toChatId"]?.toUuidOr400()
        
        val chatId = run {
          if (toChatId != null) {
            checkChatExists(call, toChatId, userId) { return@get }
            toChatId
          }
          else if (toUserId != null) {
            val memberIds = listOf(userId, toUserId).sortedWith(uuidAsStringComparator)
            val chat = checkPersonalChatExists(memberIds)
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