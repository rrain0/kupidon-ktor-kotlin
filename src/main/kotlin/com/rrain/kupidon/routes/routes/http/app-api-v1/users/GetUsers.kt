package com.rrain.kupidon.routes.routes.http.`app-api-v1`.users

import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.projectionUserM
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList




fun Application.addRouteGetUsers() {
  routing {
    get(ApiV1Routes.users) {
      val usersToApi = collUsers
        .find()
        .projectionUserM()
        .map { it.toApi(UserDataType.Stranger, call.host, call.port) }
        .toList()
      
      call.respond(mapOf(
        "users" to usersToApi,
      ))
    }
  }
}