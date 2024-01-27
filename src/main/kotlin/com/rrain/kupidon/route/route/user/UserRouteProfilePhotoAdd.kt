package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.route.util.respondInvalidBody
import com.rrain.kupidon.route.util.respondNoUserById
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMetadataMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMongo
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.util.toUuid
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import java.util.UUID




fun Application.configureUserRouteProfilePhotoAdd() {
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    authenticate {
    post(UserRoutes.postProfilePhoto) {
      val userId = call.principal<JWTPrincipal>()!!.subject!!
      val userUuid = userId.toUuid()
      
      val multipart = call.receiveMultipart()
      
      val partialPhoto = object {
        var id: UUID? = null
        var index: Int? = null
        var name: String? = null
        var mimeType: String? = null
        var binData: ByteArray? = null
      }
      
      var error: String? = null
      multipart.forEachPart {
        val prop = it.name
        when (prop) {
          "id" -> {
            if (it is PartData.FormItem) {
              partialPhoto.id = it.value.toUuid()
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
              // todo check fileName & contentType properties
              partialPhoto.binData = it.streamProvider().readBytes()
            }
            it.dispose()
          }
          
          else -> {
            error = "Unknown property '$prop'"
          }
        }
      }
      
      if (error != null) return@post call.respondInvalidBody(error)
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
        partialPhoto.mimeType!!, partialPhoto.binData!!
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
      if (photo.binData.size > 0.4*1024*1024)
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
        val nUserUpdated = UserMongo::updated.name
        
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
        if (userById.photos.any { it.index==photo.index }){
          session.abortTransaction()
          return@post call.respondInvalidBody(
            "Duplicate photo index"
          )
        }
        if (userById.photos.any { it.id==photo.id }){
          session.abortTransaction()
          return@post call.respondInvalidBody(
            "Duplicate photo id in single user"
          )
        }
        if (userById.photos.size>=6){
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
        val user = updatedUser.convertToSend(call.request)
      })
    }}
    
    
    
  }
}