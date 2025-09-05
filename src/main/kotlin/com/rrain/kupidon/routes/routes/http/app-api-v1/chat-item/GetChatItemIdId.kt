package com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-item`

import com.mongodb.ReadConcern
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UnwindOptions
import com.rrain.kupidon.models.ChatItem
import com.rrain.kupidon.models.ChatProfile
import com.rrain.kupidon.models.ChatProfileType
import com.rrain.kupidon.models.ChatType
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.CollNames
import com.rrain.kupidon.services.mongo.collChats
import com.rrain.kupidon.models.db.ChatMessageM
import com.rrain.kupidon.models.db.ChatM
import com.rrain.kupidon.routes.`convert-or-error`.toUuidOr400
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.services.mongo.findUserById
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.pathParams
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull




fun Application.addRouteGetChatItemIdId() {
  routing {
    authenticate {
      get(ApiV1Routes.chatItemIdId) {
        val userId = authUserId
        val chatId = call.pathParams["id"].toUuidOr400()
        
        val chatItem = collChats
          .withReadConcern(ReadConcern.SNAPSHOT)
          .aggregate<ChatItem>(listOf(
            Aggregates.match(
              Filters.eq(ChatItem::id.name, chatId)
            ),
            // Lookup to join chatMessages.
            // For each chat: chat.__messages = ChatMessage[]
            Aggregates.lookup(
              CollNames.chatMessages,
              ChatM::id.name,
              ChatMessageM::chatId.name,
              "__messages",
            ),
            // Unwind messages array (preserve chats with no messages)
            // For each message there is full copy of Chat object with this one message
            Aggregates.unwind(
              $$"$__messages",
              UnwindOptions().preserveNullAndEmptyArrays(true)
            ),
            // Sort messages by createdAt descending
            Aggregates.sort(
              Sorts.descending("__messages.${ChatMessageM::createdAt.name}")
            ),
            // Group by chat ID to get the latest message
            // Группировка теряет все поля, которые мы явно не указали,
            // так что сохраняем весь документ в __doc.
            // Sort order is broken after grouping!!!
            Aggregates.group(
              "$${ChatM::id.name}",
              Accumulators.first("__doc", $$$"$$ROOT")
            ),
            Aggregates.replaceRoot($$"$__doc"),
            Aggregates.set(Field("lastMessage", $$"$__messages")),
            Aggregates.unset("__messages"),
            Aggregates.sort(
              Sorts.descending("lastMessage.${ChatMessageM::createdAt.name}")
            ),
          ))
          .firstOrNull()
        
        chatItem ?: return@get call.respondNotFound(
          "NO_CHAT", "No chat by id"
        )
        
        chatItem
          .takeIf { it.type == ChatType.PERSONAL }
          ?.memberIds
          ?.find { it != userId }
          ?.let { findUserById(it) }
          ?.let {
            chatItem.profile = ChatProfile(
              id = it.id,
              type = ChatProfileType.USER,
              name = it.name,
              ava = it.ava(call.host, call.port),
            )
          }
        
        call.respond(mapOf(
          "chatItem" to chatItem.toApi(),
        ))
      }
    }
  }
}