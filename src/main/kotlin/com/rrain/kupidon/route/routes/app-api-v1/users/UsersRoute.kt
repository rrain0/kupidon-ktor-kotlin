package com.rrain.kupidon.route.routes.`app-api-v1`.users

import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.db.mongo.collUsers
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.projectionUserMongo
import com.rrain.`util-ktor`.request.getHostPort
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList




fun Application.addUsersRoute() {
  routing {
    get(ApiV1Routes.users) {
      val users = collUsers()
        .find()
        .projectionUserMongo()
        .toList()
      
      call.respond(mapOf(
        "users" to run {
          val (host, port) = call.request.getHostPort()
          users.map { it.toApi(UserDataType.Other, host, port) }
        },
      ))
    }
  }
}