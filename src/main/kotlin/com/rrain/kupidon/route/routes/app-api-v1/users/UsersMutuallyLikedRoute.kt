package com.rrain.kupidon.route.routes.`app-api-v1`.users

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Variable
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.CollNames
import com.rrain.kupidon.service.mongo.model.UserToUserLikeMongo
import com.rrain.kupidon.service.mongo.collUserToUserLikes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.UserMongo
import com.rrain.kupidon.service.mongo.model.projectionUserMongo
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import com.rrain.util.print.println
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

fun Application.addUsersMutuallyLikedRoute() {
  routing {
    authenticate {
      get(ApiV1Routes.usersMutuallyLiked) {
        val userUuid = authUserUuid
        
        val nFromUserId = UserToUserLikeMongo::fromUserId.name
        val nToUserId = UserToUserLikeMongo::toUserId.name
        
        
        val userLikesThatAreMutual = collUserToUserLikes
          .aggregate<UserToUserLikeMongo>(listOf(
            Aggregates.match(Document(nFromUserId, userUuid)),
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
        
        
        
        val mutuallyLikedUsersIds = userLikesThatAreMutual.map { it.toUserId }
        
        println("mutualLikesUserIds", mutuallyLikedUsersIds)
        
        val nUserId = UserMongo::id.name
        val likedUsers = collUsers
          .find(Filters.`in`(nUserId, mutuallyLikedUsersIds))
          .projectionUserMongo()
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