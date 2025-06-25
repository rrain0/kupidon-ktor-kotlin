package com.rrain.kupidon.route.routes.`app-api-v1`.`user-to-user`

import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.check.checkFromUserExists
import com.rrain.kupidon.route.check.checkToUserExists
import com.rrain.kupidon.route.check.checkUserToUserLikeNotExists
import com.rrain.kupidon.route.check.filterUserToUserLike
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.model.UserToUserLikeM
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.findOneOrInsert
import com.rrain.util.`date-time`.now
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
        val userId = authUserUuid
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
        
        checkFromUserExists(call, like.fromUserId) { return@post }
        checkToUserExists(call, like.toUserId) { return@post }
        checkUserToUserLikeNotExists(call, like.fromUserId, like.toUserId) { return@post }
        
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