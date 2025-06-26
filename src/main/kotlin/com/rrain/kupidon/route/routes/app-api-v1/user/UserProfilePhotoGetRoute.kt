package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Aggregates
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.route.`response-errors`.respondNotFound
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserM
import com.rrain.kupidon.model.db.UserProfilePhotoM
import com.rrain.`util-ktor`.call.queryParams
import com.rrain.util.uuid.toUuid
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document




fun Application.addUserProfilePhotoGetRoute() {
  routing {
    // https://kupidon.dev.rraindev:40002/api/v1/user/profile-photo?userId=795415da-a2cb-435b-80ee-98af28b3f0d0&photoId=3f5d4807-1112-4cdb-9eab-40c6a4e26217
    get(ApiV1Routes.userProfilePhotoName) {
      val userUuid =
        try {
          call.queryParams[ApiV1Routes.userProfilePhotoNameParams.userId]!!.toUuid()
        }
        catch (ex: Exception) {
          return@get call.respondInvalidParams("'userId' param must be uuid-string")
        }
      val photoUuid =
        try {
          call.queryParams[ApiV1Routes.userProfilePhotoNameParams.photoId]!!.toUuid()
        }
        catch (ex: Exception) {
          return@get call.respondInvalidParams("'photoId' param must be uuid-string")
        }
      
      val photo = collUsers
        .aggregate<UserProfilePhotoM>(listOf(
          Aggregates.match(Document(UserM::id.name, userUuid)),
          Aggregates.unwind("$${UserM::photos.name}"),
          Aggregates.match(Document(
            "${UserM::photos.name}.${UserProfilePhotoM::id.name}", photoUuid
          )),
          Aggregates.replaceRoot("$${UserM::photos.name}"),
          Aggregates.limit(1),
        ))
        .firstOrNull()
      
      photo ?: return@get call.respondNotFound(
        "NO_PHOTO", "Photo with such userId=$userUuid & photoId=$photoUuid was not found",
      )
      
      // Unique images have unique URL so can be cached indefinitely
      call.caching = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = Int.MAX_VALUE))
      call.respondBytes(
        ContentType.parse(photo.mimeType),
        HttpStatusCode.OK,
        suspend { photo.binData.data }
      )
    }
  }
}

