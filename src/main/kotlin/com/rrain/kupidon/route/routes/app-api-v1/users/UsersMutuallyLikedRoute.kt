package com.rrain.kupidon.route.routes.`app-api-v1`.users

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Variable
import com.rrain.kupidon.model.ChatType
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.CollNames
import com.rrain.kupidon.service.mongo.collChats
import com.rrain.kupidon.service.mongo.model.UserToUserLikeM
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.ChatM
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.kupidon.service.mongo.model.projectionUserM
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import com.rrain.util.print.println
import com.rrain.util.uuid.uuidComparator
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.bson.Document



// TODO check there is no chat between users
// TODO sort: first are newest
// TODO map to users
// TODO rename to UsersNewPairsRoute

fun Application.addRouteGetUsersMutuallyLiked() {
  routing {
    authenticate {
      get(ApiV1Routes.usersMutuallyLiked) {
        val userId = authUserUuid
        
        val nFromUserId = UserToUserLikeM::fromUserId.name
        val nToUserId = UserToUserLikeM::toUserId.name
        
        
        val userLikesThatAreMutual = collUserToUserLikes
          .aggregate<UserToUserLikeM>(listOf(
            Aggregates.match(Document(nFromUserId, userId)),
            // В текущий документ добавляется поле _mutual, содержащее результаты lookup.
            // _mutual - массив приджойненных документов.
            Aggregates.lookup(
              CollNames.userToUserLikes,
              listOf(
                Variable("fromId", "$$nFromUserId"),
                Variable("toId", "$$nToUserId"),
              ),
              listOf(
                Aggregates.match(Filters.expr(
                  Document($$"$and", listOf(
                    Document($$"$eq", listOf("$$nFromUserId", $$$"$$toId")),
                    Document($$"$eq", listOf("$$nToUserId", $$$"$$fromId")),
                  ))
                )),
              ),
              "_mutual",
            ),
            // Filter for non-empty mutual array
            Aggregates.match(Filters.ne("_mutual", emptyList<Any>())),
          ))
          .toList()
        
        
        
        var mutuallyLikedUserIds = userLikesThatAreMutual.map { it.toUserId }
        
        val chattedUserIds = collChats
          .find(Filters.and(
            Filters.eq(ChatM::type.name, ChatType.PERSONAL),
            Filters.or(
              mutuallyLikedUserIds
                .map { listOf(userId, it).sortedWith(uuidComparator) }
                .map { Filters.all(ChatM::memberIds.name, it) }
            ),
          ))
          .toList()
          .flatMap { it.memberIds }
          .toSet()
        
        mutuallyLikedUserIds = mutuallyLikedUserIds.filter { 
          it !in chattedUserIds
        }
        
        println("mutualLikesUserIds", mutuallyLikedUserIds)
        
        val likedUsers = collUsers
          .find(Filters.`in`(UserM::id.name, mutuallyLikedUserIds))
          .projectionUserM()
          .toList()
        
        call.respond(mapOf(
          "mutuallyLikedUsers" to likedUsers.map {
            it.toApi(UserDataType.Other, call.host, call.port)
          }
        ))
      }
    }
  }
}