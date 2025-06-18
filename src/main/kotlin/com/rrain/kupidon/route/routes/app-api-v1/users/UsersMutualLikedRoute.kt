package com.rrain.kupidon.route.routes.`app-api-v1`.users

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.db.mongo.model.UserToUserLikeMongo
import com.rrain.kupidon.service.db.mongo.collUserToUserLikes
import com.rrain.kupidon.service.db.mongo.collUsers
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.projectionUserMongo
import com.rrain.`util-ktor`.request.getHostPort
import com.rrain.util.print.println
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet




fun Application.addUsersMutualLikedRoute() {
  routing {
    authenticate {
      get(ApiV1Routes.usersMutualLiked) {
        val userUuid = authUserUuid
        
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
          .projectionUserMongo()
          .toList()
        
        call.respond(object {
          val likedUsers = run {
            val (host, port) = call.request.getHostPort()
            likedUsers.map { it.toApi(UserDataType.Other, host, port) }
          }
        })
      }
    }
  }
}