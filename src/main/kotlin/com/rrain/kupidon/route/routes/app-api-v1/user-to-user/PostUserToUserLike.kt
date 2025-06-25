package com.rrain.kupidon.route.routes.`app-api-v1`.`user-to-user`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.model.UserToUserLikeM
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.util.`date-time`.now
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.*




fun Application.addUserToUserLikeRoute() {
  routing {
    
    data class UserToUserLikeBodyIn(
      val toUserId: UUID,
    )
    
    authenticate {
      post(ApiV1Routes.userToUserLike) {
        val userUuid = authUserUuid
        val likeIn =
          try { call.receive<UserToUserLikeBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        
        var like = UserToUserLikeM(
          fromUserId = userUuid,
          toUserId = likeIn.toUserId,
          createdAt = now(),
        )
        
        if (like.fromUserId == like.toUserId) return@post call.respondBadRequest(
          "CANNOT_LIKE_YOURSELF", ""
        )
        
        val foundFromUser = collUsers
          .find(Filters.eq(UserM::id.name, like.fromUserId))
          .firstOrNull()
        
        foundFromUser ?: return@post call.respondBadRequest(
          "NO_FROM_USER", ""
        )
        
        val foundToUser = collUsers
          .find(Filters.eq(UserM::id.name, like.toUserId))
          .firstOrNull()
        
        foundToUser ?: return@post call.respondBadRequest(
          "NO_TO_USER", ""
        )
        
        val foundLike = collUserToUserLikes
          .find(Filters.and(
            Filters.eq(UserToUserLikeM::fromUserId.name, like.fromUserId),
            Filters.eq(UserToUserLikeM::toUserId.name, like.toUserId),
          ))
          .firstOrNull()
        
        if (foundLike != null) return@post call.respondBadRequest(
          "LIKE_ALREADY_EXISTS", ""
        )
        
        like = collUserToUserLikes.findOneOrInsert(
          Filters.and(
            Filters.eq(UserToUserLikeM::fromUserId.name, like.fromUserId),
            Filters.eq(UserToUserLikeM::toUserId.name, like.toUserId),
          ),
          like,
        )
        
        call.respond(mapOf(
          "userToUserLike" to like.toApi(),
        ))
      }
    }
  }
}