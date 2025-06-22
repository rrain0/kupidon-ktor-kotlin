package com.rrain.kupidon.route.routes.`app-api-v1`.chats

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatsMessages
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.ChatMessageMongo
import com.rrain.kupidon.service.mongo.model.ChatMongo
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import java.util.UUID



// TODO check there is no chat between users
// TODO sort: first are newest
// TODO map to users
// TODO rename to UsersNewPairsRoute

fun Application.addChatsRoute() {
  routing {
    authenticate {
      
      data class ChatBodyOut(
        val id: UUID,
        val participantsIds: List<UUID>,
        val createdAt: Instant,
        val updatedAt: Instant,
        val companionUser: MutableMap<String, Any?>? = null,
        val lastMessage: MutableMap<String, Any?>? = null,
      )
      
      get(ApiV1Routes.chats) {
        val userUuid = authUserUuid
        
        
        val chats = collChats
          .find(Filters.`in`(ChatMongo::participantsIds.name, userUuid))
          .map {
            ChatBodyOut(
              id = it.id,
              participantsIds = it.participantsIds,
              createdAt = it.createdAt,
              updatedAt = it.updatedAt,
              companionUser = collUsers
                .find(Filters.eq(it.participantsIds.find { it != userUuid }))
                .firstOrNull()
                ?.toApi(UserDataType.Other, call.host, call.port),
              lastMessage = collChatsMessages
                .find(Filters.eq(ChatMessageMongo::chatId.name, it.id))
                .sort(Sorts.descending(ChatMessageMongo::createdAt.name))
                .firstOrNull()
                ?.toApi(),
            )
          }
          .toList()
        
        call.respond(mapOf(
          "chats" to chats
        ))
      }
    }
  }
}