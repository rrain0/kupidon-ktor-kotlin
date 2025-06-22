package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.UserMongo
import com.rrain.kupidon.service.mongo.model.projectionUserMongo
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
        .find(Filters.eq(UserMongo::id.name, userUuid))
        .projectionUserMongo()
        .firstOrNull()
      
      userById ?: return@get call.respondNotFound("User with id '$userUuid' not found")
      
      return@get call.respond(mapOf(
        "user" to userById.toApi(UserDataType.Current, call.host, call.port),
      ))
    }
  }
}