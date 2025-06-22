package com.rrain.kupidon.route.routes.`app-api-v1`.`user-to-user`

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.model.UserToUserLikeMongo
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.useSingleDocTransaction
import com.rrain.util.`date-time`.zonedNow
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.*




// TODO rename to userToUserLikeRoute
fun Application.addUserToUserLikeRoute() {
  routing {
    
    data class UserToUserLikeBodyIn(
      val toUserId: UUID,
    )
    
    authenticate {
      post(ApiV1Routes.userToUserLike) {
        val userUuid = authUserUuid
        val userLikeReq = try { call.receive<UserToUserLikeBodyIn>() }
        catch (ex: Exception) { return@post call.respondInvalidBody() }
        
        
        val tryUserLike = UserToUserLikeMongo(
          fromUserId = userUuid,
          toUserId = userLikeReq.toUserId,
          // TODO DateTime
          createdAt = zonedNow(),
        )
        
        
        val userToUserLikes = collUserToUserLikes
        
        val userLike = useSingleDocTransaction { session ->
          val nFromUserId = UserToUserLikeMongo::fromUserId.name
          val nToUserId = UserToUserLikeMongo::toUserId.name
          
          // TODO проверить существуют ли юзеры
          
          suspend fun find() = userToUserLikes
            .find(
              session, Filters.and(
                Filters.eq(nFromUserId, tryUserLike.fromUserId),
                Filters.eq(nToUserId, tryUserLike.toUserId),
              )
            )
            .firstOrNull()
          
          val userLikeExisted = find()
          
          if (userLikeExisted != null) return@useSingleDocTransaction userLikeExisted
          
          userToUserLikes.insertOne(session, tryUserLike)
          
          find()!!
        }
        
        call.respond(mapOf(
          "userToUserLike" to userLike.toApi(),
        ))
      }
    }
  }
}