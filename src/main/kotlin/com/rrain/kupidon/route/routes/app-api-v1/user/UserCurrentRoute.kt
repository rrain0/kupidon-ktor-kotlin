package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.db.mongo.collUsers
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.projectionUserMongo
import com.rrain.`util-ktor`.request.getHostPort
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull




fun Application.addUserCurrentRoute() {
  routing {
    authenticate {
      get(ApiV1Routes.userCurrent) {
        val userUuid = authUserUuid
        
        val nUserId = UserMongo::id.name
        
        val userById = collUsers()
          .find(Filters.eq(nUserId, userUuid))
          .projectionUserMongo()
          .firstOrNull()
        
        userById ?: return@get call.respondNoUserById()
        
        call.respond(mapOf(
          "user" to run {
            val (host, port) = call.request.getHostPort()
            userById.toApi(UserDataType.Current, host, port)
          },
        ))
      }
    }
  }
}