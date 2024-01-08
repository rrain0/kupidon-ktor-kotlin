package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Aggregates
import com.rrain.kupidon.route.util.respondInvalidBody
import com.rrain.kupidon.route.util.respondInvalidParams
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMongo
import com.rrain.kupidon.util.SinglePathSegment
import com.rrain.kupidon.util.toUuid
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document




fun Application.configureUserRouteProfilePhoto() {
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    // https://dev.kupidon.rrain.ydns.eu:50040/api/user/profile-photo?userId=795415da-a2cb-435b-80ee-98af28b3f0d0&photoId=3f5d4807-1112-4cdb-9eab-40c6a4e26217
    get(Regex("""${UserRoutes.getProfilePhoto}$SinglePathSegment""")) {
      val userId = call.parameters[UserRoutes.getProfilePhotoParamUserId]
      val photoId = call.parameters[UserRoutes.getProfilePhotoParamPhotoId]
      
      
      userId ?: return@get call.respondInvalidParams(
        "'userId' param must be present and must be string"
      )
      photoId ?: return@get call.respondInvalidParams(
        "'photoId' param must be present and must be string"
      )
      
      val userUuid = userId.toUuid()
      val photoUuid = photoId.toUuid()
      
      val m = mongo()
      val nUserId = UserMongo::id.name
      val nUserPhotos = UserMongo::photos.name
      val nPhotosId = UserProfilePhotoMongo::id.name
      val nPhotoBinData = UserProfilePhotoMongo::binData.name
      
      val photo = m.db.coll<UserMongo>("users")
        .aggregate<UserProfilePhotoMongo>(listOf(
          Aggregates.match(Document(nUserId, userUuid)),
          /*Aggregates.match(Document(mapOf(
            nUserId to userUuid,
            "$nUserPhotos.$nPhotosId" to photoUuid,
          ))),*/
          Aggregates.unwind("$$nUserPhotos"),
          Aggregates.match(Document("$nUserPhotos.$nPhotosId", photoUuid)),
          Aggregates.replaceRoot("$$nUserPhotos"),
          Aggregates.limit(1),
        ))
        .firstOrNull()
      
      photo ?: return@get call.respond(
        HttpStatusCode.NotFound, object {
          val code = "NOT_FOUND"
          val msg = "Photo with such userId=$userId & photoId=$photoId was not found"
        }
      )
      
      call.respondBytes(
        ContentType.parse(photo.mimeType),
        HttpStatusCode.OK,
        suspend { photo.binData }
      )
    }
    
    
    
    
    post(UserRoutes.postProfilePhoto) {
      val multipart = call.receiveMultipart()
      
      val data = object {
        var index: Int? = null
        var name: String? = null
        var mimeType: String? = null
        var bytes: ByteArray? = null
      }
      
      multipart.forEachPart {
        when (it.name){
          "index" -> {
            if (it is PartData.FormItem){
              data.index = it.value.toIntOrNull()
            }
            it.dispose()
          }
          "name" -> {
            if (it is PartData.FormItem){
              data.name = it.value
            }
            it.dispose()
          }
          "mimeType" -> {
            if (it is PartData.FormItem){
              data.mimeType = it.value
            }
            it.dispose()
          }
          "bytes" -> {
            if (it is PartData.FileItem){
              // todo check fileName & contentType properties
              data.bytes = it.streamProvider().readBytes()
            }
            it.dispose()
          }
        }
      }
      
      println("index: ${data.index}")
      println("name: ${data.name}")
      println("mimeType: ${data.mimeType}")
      println("bytes?.size: ${data.bytes?.size}")
      
      if (data.index==null) return@post call.respondInvalidBody(
        "field 'index' must exist and its type must be Int"
      )
      if (data.name==null) return@post call.respondInvalidBody(
        "field 'name' must exist and its type must be String"
      )
      if (data.mimeType==null) return@post call.respondInvalidBody(
        "field 'mimeType' must exist and its type must be String"
      )
      if (data.bytes==null) return@post call.respondInvalidBody(
        "field 'bytes' must exist and its type must be File"
      )
      
      call.respond("ok")
    }
    
    
    
    
  }
}