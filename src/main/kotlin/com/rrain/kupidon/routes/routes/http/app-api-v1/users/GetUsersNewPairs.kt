package com.rrain.kupidon.routes.routes.http.`app-api-v1`.users

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.UnwindOptions
import com.mongodb.client.model.Variable
import com.rrain.kupidon.models.ChatType
import com.rrain.kupidon.models.UserPair
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.CollNames
import com.rrain.kupidon.models.db.UserToUserLikeM
import com.rrain.kupidon.services.mongo.collUserToUserLikes
import com.rrain.kupidon.models.db.ChatM
import com.rrain.kupidon.models.db.UserM
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import com.rrain.utils.base.print.println
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.Document




fun Application.addRouteGetUsersNewPairs() {
  routing {
    authenticate {
      get(ApiV1Routes.usersNewPairs) {
        val userId = authUserId
        
        val userMutualLikesWithToUserToApi = collUserToUserLikes
          .aggregate<UserPair>(listOf(
            Aggregates.match(
              Document(UserToUserLikeM::fromUserId.name, userId)
            ),
            
            // Каждому найденному лайку в поле __pair добавится обратный лайк, если он найден.
            Aggregates.lookup(
              CollNames.userToUserLikes,
              listOf(
                Variable("fromId", "$${UserToUserLikeM::fromUserId.name}"),
                Variable("toId", "$${UserToUserLikeM::toUserId.name}"),
              ),
              listOf(
                Aggregates.match(
                  Filters.expr(
                    Filters.and(
                      Document($$"$eq", listOf("$${UserToUserLikeM::fromUserId.name}", $$$"$$toId")),
                      Document($$"$eq", listOf("$${UserToUserLikeM::toUserId.name}", $$$"$$fromId")),
                    )
                  )
                ).also { println(it) }
              ),
              "__pair",
            ),
            // Drop elements which have empty list of paired likes
            Aggregates.match(Filters.ne("__pair", emptyList<Any>())),
            Aggregates.unset("__pair"),
            
            // Каждому найденному лайку в поле __chat добавится чат, если он найден
            Aggregates.lookup(
              CollNames.chats,
              listOf(
                Variable("fromId", "$${UserToUserLikeM::fromUserId.name}"),
                Variable("toId", "$${UserToUserLikeM::toUserId.name}"),
              ),
              listOf(
                Aggregates.match(
                  Filters.and(
                    Document(ChatM::type.name, ChatType.PERSONAL),
                    Filters.expr(
                      Filters.and(
                        Document($$"$in", listOf($$$"$$fromId", "$${ChatM::memberIds.name}")),
                        Document($$"$in", listOf($$$"$$toId", "$${ChatM::memberIds.name}")),
                      )
                    )
                  )
                )
              ),
              "__chat",
            ),
            // Retain elements which have no chats
            Aggregates.match(Document("__chat", emptyList<Any>())),
            Aggregates.unset("__chat"),
            
            // Каждому найденному лайку в поле toUser добавится лайкнутый пользователь
            Aggregates.lookup(
              CollNames.users,
              UserToUserLikeM::toUserId.name,
              UserM::id.name,
              UserPair::toUser.name,
            ),
            // Unpack user array to user & drop elements if user not found
            Aggregates.unwind(
              "$${UserPair::toUser.name}",
              UnwindOptions().preserveNullAndEmptyArrays(false)
            ),
            
            Aggregates.sort(Sorts.descending(UserPair::createdAt.name)),
            
          ))
          .map { it.toApi(call.host, call.port) }
          .toList()
        
        call.respond(mapOf(
          "newPairs" to userMutualLikesWithToUserToApi
        ))
      }
    }
  }
}