package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.check.checkFromUserExists
import com.rrain.kupidon.route.check.checkToUserExists
import com.rrain.kupidon.route.check.checkUserToUserLikeExists
import com.rrain.kupidon.route.check.filterNone
import com.rrain.kupidon.route.check.filterPersonalChats
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatMessages
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.kupidon.model.db.ChatMessageContentM
import com.rrain.kupidon.model.db.ChatMessageM
import com.rrain.kupidon.model.db.ChatM
import com.rrain.kupidon.service.mongo.mongoUniqueViolationRetry
import com.rrain.`util-ktor`.call.pathParams
import com.rrain.util.`date-time`.now
import com.rrain.util.uuid.toUuid
import com.rrain.util.uuid.uuidComparator
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.*



fun Application.addRoutePostChatMessageToUserIdId() {
  routing {
    
    data class ChatMessageBodyIn(
      val content: ChatMessageContentM,
    )
    
    authenticate {
      post(ApiV1Routes.chatMessageToUserIdId) {
        val userId = authUserUuid
        val toUserId = call.pathParams["id"]!!.toUuid()
        val msgIn =
          try { call.receive<ChatMessageBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        
        val now = now()
        
        var chat = ChatM(
          id = UUID.randomUUID(),
          memberIds = listOf(userId, toUserId).sortedWith(uuidComparator),
          type = ChatType.PERSONAL,
          createdAt = now,
          updatedAt = now,
        )
        
        val foundChat = collChats
          .find(filterPersonalChats(chat.memberIds))
          .firstOrNull()
        
        if (foundChat != null) chat = foundChat
        else {
          checkUserToUserLikeExists(call, userId, toUserId) { return@post }
          checkToUserExists(call, toUserId) { return@post }
          checkFromUserExists(call, userId) { return@post }
          
          mongoUniqueViolationRetry(
            {
              chat = collChats.findOneOrInsert(
                filterPersonalChats(chat.memberIds),
                chat,
              )
            },
            { chat.id = UUID.randomUUID() },
          )
        }
        
        var message = ChatMessageM(
          id = UUID.randomUUID(),
          chatId = chat.id,
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
          "chat" to chat.toApi(),
          "message" to message.toApi(),
        ))
      }
    }
  }
}