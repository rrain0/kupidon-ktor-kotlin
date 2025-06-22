package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatsMessages
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.kupidon.service.mongo.model.ChatMessageContentMongo
import com.rrain.kupidon.service.mongo.model.ChatMessageMongo
import com.rrain.kupidon.service.mongo.model.ChatMongo
import com.rrain.kupidon.service.mongo.mongoUniqueViolationRetry
import com.rrain.`util-ktor`.call.pathParams
import com.rrain.util.`date-time`.now
import com.rrain.util.uuid.toUuid
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*



fun Application.addChatMessageToUserIdIdPostRoute() {
  routing {
    
    data class ChatMessageBodyIn(
      val content: ChatMessageContentMongo,
    )
    
    authenticate {
      post(ApiV1Routes.chatMessageToUserIdId) {
        val userUuid = authUserUuid
        val toUserUuid = call.pathParams["id"]!!.toUuid()
        val msgReq = try { call.receive<ChatMessageBodyIn>() }
        catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        val now = now()
        val participantsIds = listOf(userUuid, toUserUuid)
        
        var chat = ChatMongo(
          id = UUID.randomUUID(),
          participantsIds = participantsIds,
          type = ChatType.PERSONAL,
          createdAt = now,
          updatedAt = now,
        )
        mongoUniqueViolationRetry(
          {
            chat = collChats.findOneOrInsert(
              Filters.and(
                Filters.eq(ChatMongo::type.name, ChatType.PERSONAL),
                Filters.all(ChatMongo::participantsIds.name, participantsIds),
              ),
              chat,
            )
          },
          { chat.id = UUID.randomUUID() },
        )
        
        
        var message = ChatMessageMongo(
          id = UUID.randomUUID(),
          chatId = chat.id,
          fromUserId = userUuid,
          createdAt = now,
          updatedAt = now,
          content = msgReq.content,
        )
        mongoUniqueViolationRetry(
          {
            message = collChatsMessages.findOneOrInsert(
              Filters.eq(ChatMessageMongo::id.name, message.id),
              message,
            )
          },
          { message.id = UUID.randomUUID() },
        )
        
        call.respond(mapOf(
          "chat" to chat.toApi(),
          "message" to message.toApi(),
        ))
      }
    }
  }
}