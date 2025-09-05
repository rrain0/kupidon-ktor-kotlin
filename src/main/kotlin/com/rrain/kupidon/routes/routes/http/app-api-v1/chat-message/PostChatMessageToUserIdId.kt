package com.rrain.kupidon.routes.routes.http.`app-api-v1`.`chat-message`

import com.rrain.kupidon.models.ChatType
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`check-data`.checkFromUserExists
import com.rrain.kupidon.routes.`check-data`.checkToUserExists
import com.rrain.kupidon.routes.`check-data`.filterNone
import com.rrain.kupidon.routes.`check-data`.filterPersonalChats
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.collChats
import com.rrain.kupidon.services.mongo.collChatMessages
import com.rrain.kupidon.services.mongo.findOneOrInsert
import com.rrain.kupidon.models.db.ChatMessageContentM
import com.rrain.kupidon.models.db.ChatMessageM
import com.rrain.kupidon.models.db.ChatM
import com.rrain.kupidon.routes.`check-data`.checkUsersPairExists
import com.rrain.kupidon.routes.`convert-or-error`.toUuidOr400
import com.rrain.kupidon.services.mongo.mongoUniqueViolationRetry
import com.rrain.utils.ktor.call.pathParams
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.base.uuid.uuidAsStringComparator
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
        val userId = authUserId
        val toUserId = call.pathParams["id"].toUuidOr400()
        val msgIn =
          try { call.receive<ChatMessageBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        
        val now = now()
        
        var chat = ChatM(
          id = UUID.randomUUID(),
          memberIds = listOf(userId, toUserId).sortedWith(uuidAsStringComparator),
          type = ChatType.PERSONAL,
          createdAt = now,
          updatedAt = now,
        )
        
        val foundChat = collChats
          .find(filterPersonalChats(chat.memberIds))
          .firstOrNull()
        
        if (foundChat != null) chat = foundChat
        else {
          checkUsersPairExists(userId, toUserId)
          checkToUserExists(toUserId)
          checkFromUserExists(userId)
          
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