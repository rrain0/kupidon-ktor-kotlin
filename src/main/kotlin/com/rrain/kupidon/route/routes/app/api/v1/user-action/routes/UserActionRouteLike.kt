package com.rrain.kupidon.route.routes.app.api.v1.`user-action`.routes

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.getUserId
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.app.api.v1.`user-action`.UserActionRoutes
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.service.db.mongo.model.UserToUserLikeMongo
import com.rrain.kupidon.service.db.mongo.mongo
import com.rrain.kupidon.service.db.mongo.collUserToUserLikes
import com.rrain.kupidon.service.db.mongo.collUsers
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.projectUserMongo
import com.rrain.`util-ktor`.request.getHostPort
import com.rrain.util.`date-time`.zonedNow
import com.rrain.util.print.println
import com.rrain.util.uuid.toUuid
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import java.util.*



// TODO rename to userToUserLikeRoute
fun Application.configureUserActionRouteLike() {
  
  
  
  
  routing {
    
    
    
    data class UserToUserLikeReq(
      val toUserId: UUID,
    )
    
    authenticate {
      post(UserActionRoutes.base) {
        val userUuid = call.getUserId().toUuid()
        val userLikeReq = try {
          call.receive<UserToUserLikeReq>()
        }
        catch (ex: Exception) {
          return@post call.respondInvalidBody()
        }
        
        
        val tryUserLike = UserToUserLikeMongo(
          fromUserId = userUuid,
          toUserId = userLikeReq.toUserId,
          // TODO DateTime
          created = zonedNow(),
        )
        
        
        val m = mongo()
        val userToUserLikes = collUserToUserLikes()
        
        val userLike = m.useTransaction { session ->
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
            .limit(1)
            .firstOrNull()
          
          val userLikeExisted = find()
          
          if (userLikeExisted != null) return@useTransaction userLikeExisted
          
          userToUserLikes.insertOne(session, tryUserLike)
          
          find()!!
        }
        
        call.respond(object {
          val userToUserLike = userLike.convertToSend()
        })
      }
    }
    
    authenticate {
      get(UserActionRoutes.listAll) {
        val userUuid = call.getUserId().toUuid()
        
        val nFromUserId = UserToUserLikeMongo::fromUserId.name
        val likedUsersIds = collUserToUserLikes()
          .find(Filters.eq(nFromUserId, userUuid))
          .map { it.toUserId }
          .toSet()
        
        println("likedUsersIds", likedUsersIds)
        
        val nToUserId = UserToUserLikeMongo::toUserId.name
        val userLikedBy = collUserToUserLikes()
          .find(Filters.eq(nToUserId, userUuid))
          .map { it.fromUserId }
          .toSet()
        
        println("userLikedBy", userLikedBy)
        
        val mutualLikesUserIds = likedUsersIds.intersect(userLikedBy)
        
        println("mutualLikesUserIds", mutualLikesUserIds)
        
        val nUserId = UserMongo::id.name
        val likedUsers = collUsers()
          .find(Filters.`in`(nUserId, mutualLikesUserIds))
          .projectUserMongo()
          .toList()
        
        call.respond(object {
          val likedUsers = run {
            val (host, port) = call.request.getHostPort()
            likedUsers.map { it.convertToSend(UserDataType.Other, host, port) }
          }
        })
      }
    }
    
    
    
  }
}