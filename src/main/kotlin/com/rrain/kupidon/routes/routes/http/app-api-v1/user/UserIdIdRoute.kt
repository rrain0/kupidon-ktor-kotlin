package com.rrain.kupidon.routes.routes.http.`app-api-v1`.user

import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.routes.`convert-or-error`.toUuidOr400
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.services.mongo.findUserById
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.pathParams
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addUserIdIdRoute() {
  routing {
    get(ApiV1Routes.userIdId) {
      val userId = call.pathParams["id"].toUuidOr400()
      
      val user = findUserById(userId)
      
      user ?: return@get call.respondNotFound(
        "NO_USER", "User with such id not found"
      )
      
      // TODO send user as stranger
      return@get call.respond(mapOf(
        "user" to user.toApi(UserDataType.Current, call.host, call.port),
      ))
    }
  }
}