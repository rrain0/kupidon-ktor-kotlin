package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserDataType
import com.rrain.kupidon.model.db.UserM
import com.rrain.kupidon.model.db.projectionUserM
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
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
        
        val nUserId = UserM::id.name
        
        val userById = collUsers
          .find(Filters.eq(nUserId, userUuid))
          .projectionUserM()
          .firstOrNull()
        
        userById ?: return@get call.respondNoUserById()
        
        call.respond(mapOf(
          "user" to userById.toApi(UserDataType.Current, call.host, call.port),
        ))
      }
    }
  }
}