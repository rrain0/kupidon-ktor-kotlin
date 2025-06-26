package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-items`

import com.mongodb.ReadConcern
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UnwindOptions
import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.CollNames
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.ChatMessageM
import com.rrain.kupidon.model.db.ChatM
import com.rrain.kupidon.model.db.UserM
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import java.util.UUID



//enum class ChatProfileType { USER }

data class ChatProfile(
  val id: UUID,
  //val type: ChatProfileType,
  val name: String,
  val ava: String,
  // val online
  // val lastOnlineAt
)

data class ChatItem(
  var id: UUID,
  var type: ChatType,
  var memberIds: List<UUID>,
  var createdAt: Instant,
  var updatedAt: Instant,
  var profile: ChatProfile? = null,
  //var companionUser: MutableMap<String, Any?>? = null,
  var lastMessage: ChatMessageM? = null,
) {
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "id" to id,
      "memberIds" to memberIds,
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
      "profile" to profile,
      "lastMessage" to lastMessage?.toApi(),
    )
  }
}




// TODO check there is no chat between users
// TODO sort: first are newest
// TODO map to users
// TODO rename to UsersNewPairsRoute

fun Application.addChatItemsGetRoute() {
  routing {
    authenticate {
      get(ApiV1Routes.chatItems) {
        val userUuid = authUserUuid
        
        val chatItems = collChats
          .withReadConcern(ReadConcern.SNAPSHOT)
          .aggregate<ChatItem>(listOf(
            Aggregates.match(
              Filters.all(ChatItem::memberIds.name, userUuid)
            ),
            // Lookup to join chatMessages.
            // For each chat chat.__messages = ChatMessage[]
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
            Aggregates.sort(Sorts.orderBy(
              Sorts.descending("__messages.${ChatMessageM::createdAt.name}")
            )),
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
          .toList()
        
        println("chatItems 1: $chatItems")
        
        chatItems.onEach {
          if (it.type == ChatType.PERSONAL) {
            it.profile = collUsers
              .find(Filters.eq(
                UserM::id.name,
                it.memberIds
                  .also { println("memberIds: $it") }
                  .also { println("userUuid: $userUuid") }
                  .find { it != userUuid }!!.also { println("companion uuid: $it") }
              ))
              .firstOrNull()
              ?.let {
                ChatProfile(
                  id = it.id,
                  //type = ChatProfileType.USER,
                  name = it.name,
                  ava = it.photos
                    .find { it.index == 0 }
                    ?.getUrl(it.id, call.host, call.port)
                    ?: "",
                )
              }
          }
        }
        
        println("chatItems 2: $chatItems")
        
        
        call.respond(mapOf(
          "chatItems" to chatItems,
        ))
      }
    }
  }
}