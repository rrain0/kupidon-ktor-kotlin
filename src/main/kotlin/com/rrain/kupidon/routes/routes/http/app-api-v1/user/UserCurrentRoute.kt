package com.rrain.kupidon.routes.routes.http.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`response-errors`.respondNoUserById
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.projectionUserM
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull




fun Application.addUserCurrentRoute() {
  routing {
    authenticate {
      get(ApiV1Routes.userCurrent) {
        val userUuid = authUserId
        
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