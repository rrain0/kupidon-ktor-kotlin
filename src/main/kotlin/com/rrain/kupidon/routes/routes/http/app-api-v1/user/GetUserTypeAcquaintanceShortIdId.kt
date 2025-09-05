package com.rrain.kupidon.routes.routes.http.`app-api-v1`.user

import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`convert-or-error`.toUuidOr400
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.services.mongo.findUserById
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.pathParams
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addRouteGetUserTypeAcquaintanceShortIdId() {
  routing {
    // TODO make request to chat
    authenticate {
      get(ApiV1Routes.userTypeAcquaintanceShortIdId) {
        val authId = authUserId
        val userId = call.pathParams["id"].toUuidOr400()
        
        // TODO check if user has pair or chat with requested user
        val user = findUserById(userId)
        
        user ?: return@get call.respondNotFound(
          "NO_USER", "User with such id not found"
        )
        
        return@get call.respond(
          mapOf(
            "user" to user.toApi(UserDataType.AcquaintanceShort, call.host, call.port),
          )
        )
      }
    }
  }
}