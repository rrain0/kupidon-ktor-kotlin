package com.rrain.kupidon.route.routes.`app-api-v1`.users

import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserDataType
import com.rrain.kupidon.model.db.projectionUserM
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
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
        .map { it.toApi(UserDataType.Other, call.host, call.port) }
        .toList()
      
      call.respond(mapOf(
        "users" to usersToApi,
      ))
    }
  }
}