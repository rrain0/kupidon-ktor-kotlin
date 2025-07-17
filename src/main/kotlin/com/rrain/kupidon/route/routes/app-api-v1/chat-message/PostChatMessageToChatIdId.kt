package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.rrain.kupidon.plugin.authUserId
import com.rrain.kupidon.route.`check-data`.checkChatExists
import com.rrain.kupidon.route.`check-data`.filterNone
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChatMessages
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.kupidon.model.db.ChatMessageContentM
import com.rrain.kupidon.model.db.ChatMessageM
import com.rrain.kupidon.route.`convert-or-error`.toUuidOr400
import com.rrain.kupidon.service.mongo.mongoUniqueViolationRetry
import com.rrain.util.ktor.call.pathParams
import com.rrain.util.base.`date-time`.now
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*



fun Application.addRoutePostChatMessageToChatIdId() {
  routing {
    
    data class ChatMessageBodyIn(
      val content: ChatMessageContentM,
    )
    
    authenticate {
      post(ApiV1Routes.chatMessageToChatIdId) {
        val userId = authUserId
        val toChatId = call.pathParams["id"].toUuidOr400()
        val msgIn =
          try { call.receive<ChatMessageBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        val now = now()
        
        val foundChat = checkChatExists(call, toChatId, userId) { return@post }
        
        var message = ChatMessageM(
          id = UUID.randomUUID(),
          chatId = foundChat.id,
          fromUserId = userId,
          createdAt = now,
          updatedAt = now,
          content = msgIn.content,
        )
        mongoUniqueViolationRetry(
          {
            message = collChatMessages.findOneOrInsert(
              // Condition that always matches none to guarantee insertion
              filterNone(),
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