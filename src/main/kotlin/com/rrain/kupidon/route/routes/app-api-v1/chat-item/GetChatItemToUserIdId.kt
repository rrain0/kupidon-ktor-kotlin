package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-item`

import com.mongodb.ReadConcern
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UnwindOptions
import com.rrain.kupidon.model.ChatItem
import com.rrain.kupidon.model.ChatProfile
import com.rrain.kupidon.model.ChatProfileType
import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.plugin.authUserId
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.CollNames
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.model.db.ChatMessageM
import com.rrain.kupidon.model.db.ChatM
import com.rrain.kupidon.route.`check-data`.checkUsersPairExists
import com.rrain.kupidon.route.`check-data`.filterPersonalChats
import com.rrain.kupidon.route.`convert-or-error`.toUuidOr400
import com.rrain.kupidon.service.mongo.findUserById
import com.rrain.util.base.`date-time`.now
import com.rrain.util.base.uuid.NilUuid
import com.rrain.util.base.uuid.uuidComparator
import com.rrain.util.ktor.call.host
import com.rrain.util.ktor.call.pathParams
import com.rrain.util.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull




fun Application.addRouteGetChatItemToUserIdId() {
  routing {
    authenticate {
      get(ApiV1Routes.chatItemToUserIdId) {
        val userId = authUserId
        val toUserId = call.pathParams["id"].toUuidOr400()
        
        val now = now()
        val memberIds = listOf(userId, toUserId).sortedWith(uuidComparator)
        
        val foundChat = collChats
          .withReadConcern(ReadConcern.SNAPSHOT)
          .aggregate<ChatItem>(listOf(
            Aggregates.match(
              filterPersonalChats(memberIds)
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
        
        if (foundChat != null) {
          foundChat
            .takeIf { it.type == ChatType.PERSONAL }
            ?.memberIds
            ?.find { it != userId }
            ?.let { findUserById(it) }
            ?.let {
              foundChat.profile = ChatProfile(
                id = it.id,
                type = ChatProfileType.USER,
                name = it.name,
                ava = it.ava(call.host, call.port),
              )
            }
          
          call.respond(mapOf(
            "chatItem" to foundChat.toApi(),
          ))
        }
        
        checkUsersPairExists(userId, toUserId)
        
        val toUser = findUserById(toUserId)
        
        // Case when you can write to user, but chat was not created yet.
        call.respond(mapOf(
          "chatItem" to ChatItem(
            id = NilUuid,
            type = ChatType.PERSONAL,
            memberIds = memberIds,
            createdAt = now,
            updatedAt = now,
            profile = toUser?.let {
              ChatProfile(
                id = it.id,
                type = ChatProfileType.USER,
                name = it.name,
                ava = it.ava(call.host, call.port),
              )
            },
            lastMessage = null,
          ).toApi()
        ))
      }
    }
  }
}