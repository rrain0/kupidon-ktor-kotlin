package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.service.db.mongo.collUsers
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMongo
import com.rrain.kupidon.service.db.mongo.model.projectionUserMongo
import com.rrain.`util-ktor`.request.getHostPort
import com.rrain.util.uuid.toUuid
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document



fun Application.addUserIdIdRoute() {
  routing {
    get(ApiV1Routes.userIdId) {
      val userUuid = try {
        call.request.pathVariables["id"]!!.toUuid()
      }
      catch (ex: Exception) {
        return@get call.respondInvalidParams("'id' path param must be uuid-string")
      }
      
      val nUserId = UserMongo::id.name
      
      val userById = collUsers()
        .find(Filters.eq(nUserId, userUuid))
        .projectionUserMongo()
        .firstOrNull()
      
      userById ?: return@get call.respondNoUserById()
      
      return@get call.respond(mapOf(
        "user" to run {
          val (host, port) = call.request.getHostPort()
          userById.toApi(UserDataType.Current, host, port)
        },
      ))
    }
  }
}