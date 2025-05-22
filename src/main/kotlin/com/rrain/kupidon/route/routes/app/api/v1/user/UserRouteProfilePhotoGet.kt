package com.rrain.kupidon.route.routes.app.api.v1.user

import com.mongodb.client.model.Aggregates
import com.rrain.kupidon.route.`response-errors`.respondInvalidParams
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMongo
import com.rrain.util.url.urlSinglePathSegmentPattern
import com.rrain.util.uuid.toUuid
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document




fun Application.configureUserRouteProfilePhotoGet() {
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    // https://dev.kupidon.rrain.ydns.eu:50040/api/user/profile-photo?userId=795415da-a2cb-435b-80ee-98af28b3f0d0&photoId=3f5d4807-1112-4cdb-9eab-40c6a4e26217
    get(Regex("""${UserRoutes.getProfilePhoto}$urlSinglePathSegmentPattern""")) {
      val userUuid = try { call.parameters[UserRoutes.getProfilePhotoParamUserId]!!.toUuid() }
      catch (ex: Exception) {
        return@get call.respondInvalidParams("'userId' param must be uuid-string")
      }
      val photoUuid = try { call.parameters[UserRoutes.getProfilePhotoParamPhotoId]!!.toUuid() }
      catch (ex: Exception) {
        return@get call.respondInvalidParams("'photoId' param must be uuid-string")
      }
      
      val m = mongo()
      val nUserId = UserMongo::id.name
      val nUserPhotos = UserMongo::photos.name
      val nPhotoId = UserProfilePhotoMongo::id.name
      val nPhotoBinData = UserProfilePhotoMongo::binData.name
      
      val photo = m.db.coll<UserMongo>("users")
        .aggregate<UserProfilePhotoMongo>(listOf(
          Aggregates.match(Document(nUserId, userUuid)),
          Aggregates.unwind("$$nUserPhotos"),
          Aggregates.match(Document("$nUserPhotos.$nPhotoId", photoUuid)),
          Aggregates.replaceRoot("$$nUserPhotos"),
          Aggregates.limit(1),
        ))
        .firstOrNull()
      
      photo ?: return@get call.respond(
        HttpStatusCode.NotFound, object {
          val code = "NOT_FOUND"
          val msg = "Photo with such userId=$userUuid & photoId=$photoUuid was not found"
        }
      )
      
      // Unique images have unique URL so can cache indefinitely
      call.caching = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = Int.MAX_VALUE))
      call.respondBytes(
        ContentType.parse(photo.mimeType),
        HttpStatusCode.OK,
        suspend { photo.binData.data }
      )
    }
    
    
    
    
  }
}