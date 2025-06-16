package com.rrain.kupidon.route.routes.app.api.v1.user.routes

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.plugin.getUserId
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMetadataMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMongo
import com.rrain.kupidon.service.db.mongo.mongo
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.route.routes.app.api.v1.user.UserRoutes
import com.rrain.`util-ktor`.request.getHostPort
import com.rrain.util.uuid.toUuid
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import org.bson.types.Binary
import java.util.UUID




fun Application.configureUserRouteProfilePhotoAdd() {
  
  
  
  routing {
    authenticate {
      post(UserRoutes.postProfilePhoto) {
        val userUuid = call.getUserId().toUuid()
        
        val multipart = call.receiveMultipart()
        
        val partialPhoto = object {
          var id: UUID? = null
          var index: Int? = null
          var name: String? = null
          var mimeType: String? = null
          var binData: ByteArray? = null
        }
        
        var unknownPropError: String? = null
        var invalidParamError: String? = null
        multipart.forEachPart {
          if (unknownPropError != null || invalidParamError != null) return@forEachPart
          val prop = it.name
          when (prop) {
            "id" -> {
              if (it is PartData.FormItem) {
                partialPhoto.id = try { it.value.toUuid() }
                catch (ex: Exception) {
                  invalidParamError = "'id' must be uuid-string"
                  return@forEachPart
                }
              }
              it.dispose()
            }
            
            "index" -> {
              if (it is PartData.FormItem) {
                partialPhoto.index = it.value.toIntOrNull()
              }
              it.dispose()
            }
            
            "name" -> {
              if (it is PartData.FormItem) {
                partialPhoto.name = it.value
              }
              it.dispose()
            }
            
            "mimeType" -> {
              if (it is PartData.FormItem) {
                partialPhoto.mimeType = it.value
              }
              it.dispose()
            }
            
            "binData" -> {
              if (it is PartData.FileItem) {
                partialPhoto.binData = it.provider().toByteArray()
              }
              it.dispose()
            }
            
            else -> {
              unknownPropError = "Unknown property '$prop'"
              return@forEachPart
            }
          }
        }
        
        if (unknownPropError != null) return@post call.respondInvalidBody(unknownPropError)
        if (invalidParamError != null) return@post call.respondInvalidBody(invalidParamError)
        if (partialPhoto.index == null) return@post call.respondInvalidBody(
          "field 'index' must exist and its type must be Int"
        )
        if (partialPhoto.name == null) return@post call.respondInvalidBody(
          "field 'name' must exist and its type must be String"
        )
        if (partialPhoto.mimeType == null) return@post call.respondInvalidBody(
          "field 'mimeType' must exist and its type must be String"
        )
        if (partialPhoto.binData == null) return@post call.respondInvalidBody(
          "field 'bytes' must exist and its type must be File"
        )
        
        //println("id: ${partialPhoto.id}")
        //println("index: ${partialPhoto.index}")
        //println("name: ${partialPhoto.name}")
        //println("mimeType: ${partialPhoto.mimeType}")
        //println("bytes?.size: ${partialPhoto.bytes?.size}")
        
        val photo = UserProfilePhotoMongo(
          partialPhoto.id!!, partialPhoto.index!!, partialPhoto.name!!,
          partialPhoto.mimeType!!, Binary(partialPhoto.binData!!)
        )
        
        if (photo.index !in 0..5) return@post call.respondInvalidBody(
          "photo index must be in range ${0..5}"
        )
        if (photo.name.length > 256) return@post call.respondInvalidBody(
          "photo name max length must be 256 chars"
        )
        if (!photo.mimeType.startsWith("image/")) return@post call.respondInvalidBody(
          "photo must have mime-type starting with 'image/', " +
            "but yours is '${photo.mimeType}'"
        )
        /*if (photo.mimeType!="image/webp") return@post call.respondInvalidBody(
          "photo mimeType must be 'image/webp'"
        )*/
        if (photo.binData.data.size > 0.4 * 1024 * 1024)
          return@post call.respondInvalidBody(
            "photo bytes max size must be 0.4MB, " +
              "but yours is ${photo.binData} bytes"
          )
        
        
        
        
        val m = mongo()
        val updatedUser = m.useTransaction { session ->
          val nUserId = UserMongo::id.name
          val nUserPwd = UserMongo::pwd.name
          val nUserName = UserMongo::name.name
          val nUserBirthDate = UserMongo::birthDate.name
          val nUserGender = UserMongo::gender.name
          val nUserAboutMe = UserMongo::aboutMe.name
          val nUserPhotos = UserMongo::photos.name
          val nUserUpdated = UserMongo::updatedAt.name
          
          val nPhotoId = UserProfilePhotoMetadataMongo::id.name
          val nPhotoIndex = UserProfilePhotoMetadataMongo::index.name
          val nPhotoBinData = UserProfilePhotoMongo::binData.name
          
          
          val userById = m.db.coll<UserMongo>("users")
            .find(session, Filters.eq(nUserId, userUuid))
            .projection(Document("$nUserPhotos.$nPhotoBinData", false))
            .limit(1)
            .firstOrNull()
          
          if (userById == null) {
            session.abortTransaction()
            return@post call.respondNoUserById()
          }
          if (userById.photos.any { it.index == photo.index }){
            session.abortTransaction()
            return@post call.respondInvalidBody(
              "Duplicate photo index"
            )
          }
          if (userById.photos.any { it.id == photo.id }){
            session.abortTransaction()
            return@post call.respondInvalidBody(
              "Duplicate photo id in single user"
            )
          }
          if (userById.photos.size >= 6){
            session.abortTransaction()
            return@post call.respondInvalidBody(
              "Maximum photos count is 6"
            )
          }
          
          m.db.coll<UserMongo>("users")
          .updateOne(session,
            Filters.eq(nUserId, userUuid),
            Updates.combine(
              Updates.pushEach(nUserPhotos, listOf(photo)),
              Updates.currentDate(nUserUpdated)
            )
          )
          
          val updatedUser = m.db.coll<UserMongo>("users")
            .find(session, Filters.eq(UserMongo::id.name, userUuid))
            .projection(Document("$nUserPhotos.$nPhotoBinData", false))
            .first()
          
          updatedUser
        }
        
        call.respond(object {
          val user = run {
            val (host, port) = call.request.getHostPort()
            updatedUser.convertToSend(UserDataType.Current, host, port)
          }
        })
      }
    }
  }
}