package com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.collChatMessages
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.kupidon.service.mongo.model.ChatMessageContentM
import com.rrain.kupidon.service.mongo.model.ChatMessageM
import com.rrain.kupidon.service.mongo.model.ChatM
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.kupidon.service.mongo.model.UserToUserLikeM
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
        val userUuid = authUserUuid
        val toUserUuid = call.pathParams["id"]!!.toUuid()
        val msgIn =
          try { call.receive<ChatMessageBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        
        val now = now()
        
        var chat = ChatM(
          id = UUID.randomUUID(),
          memberIds = listOf(userUuid, toUserUuid).sortedWith(uuidComparator),
          type = ChatType.PERSONAL,
          createdAt = now,
          updatedAt = now,
        )
        
        val foundChat = collChats
          .find(Filters.and(
            Filters.eq(ChatM::type.name, ChatType.PERSONAL),
            Filters.eq(ChatM::memberIds.name, chat.memberIds),
          ))
          .firstOrNull()
        
        if (foundChat != null) chat = foundChat
        else {
          val foundLike = collUserToUserLikes
            .find(Filters.and(
              Filters.eq(UserToUserLikeM::fromUserId.name, userUuid),
              Filters.eq(UserToUserLikeM::toUserId.name, toUserUuid),
            ))
            .firstOrNull()
          
          foundLike ?: return@post call.respondBadRequest(
            "NO_MUTUAL_LIKE", "You have no chat with this user and have no mutual like"
          )
          
          val foundToUser = collUsers
            .find(Filters.eq(UserM::id.name, toUserUuid))
            .firstOrNull()
          
          foundToUser ?: return@post call.respondBadRequest(
            "NO_TO_USER"
          )
          
          val foundFromUser = collUsers
            .find(Filters.eq(UserM::id.name, userUuid))
            .firstOrNull()
          
          foundFromUser ?: return@post call.respondBadRequest(
            "NO_FROM_USER"
          )
          
          mongoUniqueViolationRetry(
            {
              chat = collChats.findOneOrInsert(
                Filters.and(
                  Filters.eq(ChatM::type.name, ChatType.PERSONAL),
                  Filters.eq(ChatM::memberIds.name, chat.memberIds),
                ),
                chat,
              )
            },
            { chat.id = UUID.randomUUID() },
          )
        }
        
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
              // Condition that always matches none to guarantee insertion
              // UUID is not string by type
              Filters.eq(ChatMessageM::id.name, ""),
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