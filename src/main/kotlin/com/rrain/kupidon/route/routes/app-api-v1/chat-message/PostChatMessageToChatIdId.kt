package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatMessages
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.kupidon.service.mongo.model.ChatMessageContentM
import com.rrain.kupidon.service.mongo.model.ChatMessageM
import com.rrain.kupidon.service.mongo.model.ChatM
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



fun Application.addRoutePostChatMessageToChatIdId() {
  routing {
    
    data class ChatMessageBodyIn(
      val content: ChatMessageContentM,
    )
    
    authenticate {
      post(ApiV1Routes.chatMessageToChatIdId) {
        val userUuid = authUserUuid
        val toChatUuid = call.pathParams["id"]!!.toUuid()
        val msgIn =
          try { call.receive<ChatMessageBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        val now = now()
        
        val chat = collChats
          .find(Filters.eq(ChatM::id.name, toChatUuid))
          .firstOrNull()
        
        chat ?: return@post call.respondNotFound(
          "NO_CHAT", "Chat with id '$toChatUuid' not found"
        )
        
        var message = ChatMessageM(
          id = UUID.randomUUID(),
          chatId = chat.id,
          fromUserId = userUuid,
          createdAt = now,
          updatedAt = now,
          content = msgIn.content,
        )
        mongoUniqueViolationRetry(
          {
            message = collChatMessages.findOneOrInsert(
              Filters.eq(ChatMessageM::id.name, message.id),
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