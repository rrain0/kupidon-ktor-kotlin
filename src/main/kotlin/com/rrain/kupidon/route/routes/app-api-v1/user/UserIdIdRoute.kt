package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserDataType
import com.rrain.kupidon.model.db.UserM
import com.rrain.kupidon.model.db.projectionUserM
import com.rrain.kupidon.route.`convert-or-error`.toUuidOr400
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.pathParams
import com.rrain.`util-ktor`.call.port
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull



fun Application.addUserIdIdRoute() {
  routing {
    get(ApiV1Routes.userIdId) {
      val userUuid = call.pathParams["id"].toUuidOr400()
      
      val userById = collUsers
        .find(Filters.eq(UserM::id.name, userUuid))
        .projectionUserM()
        .firstOrNull()
      
      userById ?: return@get call.respondBadRequest(
        "NO_USER", "User with id '$userUuid' not found"
      )
      
      return@get call.respond(mapOf(
        "user" to userById.toApi(UserDataType.Current, call.host, call.port),
      ))
    }
  }
}