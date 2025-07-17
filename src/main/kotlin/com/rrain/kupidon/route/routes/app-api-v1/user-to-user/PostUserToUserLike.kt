package com.rrain.kupidon.route.routes.`app-api-v1`.`user-to-user`

import com.rrain.kupidon.plugin.authUserId
import com.rrain.kupidon.route.`check-data`.checkFromUserExists
import com.rrain.kupidon.route.`check-data`.checkToUserExists
import com.rrain.kupidon.route.`check-data`.checkUserToUserLikeNotExists
import com.rrain.kupidon.route.`check-data`.filterUserToUserLike
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.model.db.UserToUserLikeM
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.util.base.`date-time`.now
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*




fun Application.addRoutePostUserToUserLike() {
  routing {
    
    data class UserToUserLikeBodyIn(
      val toUserId: UUID,
    )
    
    authenticate {
      post(ApiV1Routes.userToUserLike) {
        val userId = authUserId
        val likeIn =
          try { call.receive<UserToUserLikeBodyIn>() }
          catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        
        var like = UserToUserLikeM(
          fromUserId = userId,
          toUserId = likeIn.toUserId,
          createdAt = now(),
        )
        
        if (like.fromUserId == like.toUserId) return@post call.respondBadRequest(
          "CANNOT_LIKE_YOURSELF", ""
        )
        
        checkFromUserExists(like.fromUserId)
        checkToUserExists(like.toUserId)
        checkUserToUserLikeNotExists(like.fromUserId, like.toUserId)
        
        like = collUserToUserLikes.findOneOrInsert(
          filterUserToUserLike(like.fromUserId, like.toUserId),
          like,
        )
        
        call.respond(mapOf(
          "userToUserLike" to like.toApi(),
        ))
      }
    }
  }
}