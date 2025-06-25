package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.kupidon.service.mongo.model.projectionUserM
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.pathParams
import com.rrain.`util-ktor`.call.port
import com.rrain.util.uuid.toUuid
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull



fun Application.addUserIdIdRoute() {
  routing {
    get(ApiV1Routes.userIdId) {
      val userUuid = try { call.pathParams["id"]!!.toUuid() }
      catch (ex: Exception) {
        return@get call.respondInvalidParams("'id' path param must be UUID-string")
      }
      
      val userById = collUsers
        .find(Filters.eq(UserM::id.name, userUuid))
        .projectionUserM()
        .firstOrNull()
      
      userById ?: return@get call.respondNotFound(
        "NO_USER", "User with id '$userUuid' not found"
      )
      
      return@get call.respond(mapOf(
        "user" to userById.toApi(UserDataType.Current, call.host, call.port),
      ))
    }
  }
}