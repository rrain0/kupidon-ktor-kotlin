package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.`response-errors`.respondNotFound
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
import kotlinx.coroutines.flow.firstOrNull
import java.util.*



fun Application.addChatMessageToChatIdIdPostRoute() {
  routing {
    
    data class ChatMessageBodyIn(
      val content: ChatMessageContentMongo,
    )
    
    authenticate {
      post(ApiV1Routes.chatMessageToChatIdId) {
        val userUuid = authUserUuid
        val toChatUuid = call.pathParams["id"]!!.toUuid()
        val msgBodyIn =
          try { call.receive<ChatMessageBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        val now = now()
        
        val chat = collChats
          .find(Filters.eq(ChatMongo::id.name, toChatUuid))
          .firstOrNull()
        
        chat ?: return@post call.respondNotFound(
          "Chat with id '$toChatUuid' not found"
        )
        
        
        var message = ChatMessageMongo(
          id = UUID.randomUUID(),
          chatId = chat.id,
          fromUserId = userUuid,
          createdAt = now,
          updatedAt = now,
          content = msgBodyIn.content,
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
          "message" to message.toApi(),
        ))
      }
    }
  }
}