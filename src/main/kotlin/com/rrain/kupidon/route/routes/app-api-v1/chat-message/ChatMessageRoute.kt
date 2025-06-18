package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.db.mongo.collChats
import com.rrain.kupidon.service.db.mongo.collChatsMessages
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.service.db.mongo.mongo
import com.rrain.kupidon.service.db.mongo.model.ChatMessageContentMongo
import com.rrain.kupidon.service.db.mongo.model.ChatMessageMongo
import com.rrain.kupidon.service.db.mongo.model.ChatMongo
import com.rrain.util.`date-time`.zonedNow
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.*



fun Application.addChatMessageRoute() {
  routing {
    
    data class ChatMessageBodyIn(
      val toUserId: UUID,
      val content: ChatMessageContentMongo,
    )
    
    authenticate {
      post(ApiV1Routes.chatMessage) {
        val userUuid = authUserUuid
        val msgReq = try {
          call.receive<ChatMessageBodyIn>()
        }
        catch (ex: Exception) {
          return@post call.respondInvalidBody()
        }
        
        
        val m = mongo()
        val chats = collChats()
        val chatsMessages = collChatsMessages()
        
        val message = m.useTransaction { session ->
          val nParticipantsIds = ChatMongo::participantsIds.name
          
          val now = zonedNow()
          val participantsIds = listOf(userUuid, msgReq.toUserId)
          
          var chat = chats
            .find(
              Filters.and(
                Filters.all(nParticipantsIds, participantsIds),
                Filters.size(nParticipantsIds, 2)
              )
            )
            .firstOrNull()
          
          if (chat == null) {
            chat = ChatMongo(
              id = UUID.randomUUID(),
              participantsIds = participantsIds,
              createdAt = now,
              updatedAt = now,
            )
            chats.insertOne(chat)
          }
          
          val message = ChatMessageMongo(
            id = UUID.randomUUID(),
            chatId = chat.id,
            fromUserId = userUuid,
            createdAt = now,
            updatedAt = now,
            content = msgReq.content,
          )
          
          chatsMessages.insertOne(message)
          
          message
        }
        
        call.respond(mapOf(
          "message" to message.toApi(),
        ))
      }
    }
  }
}