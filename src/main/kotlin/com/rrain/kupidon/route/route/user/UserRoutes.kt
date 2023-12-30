package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.util.respondNoUserById
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMongo
import com.rrain.kupidon.util.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document




object UserRoutes {
  const val base = "/api/user"
  const val current = "$base/current"
  const val create = "$base/create"
  const val update = "$base/update"
  const val emailInitialVerification = "$base/verify/initial-email"
  const val getById = "$base/get-by-id/{id}"
  
  const val getProfilePhoto = "$base/profile-photo"
  const val getProfilePhotoParamUserId = "userId"
  const val getProfilePhotoParamPhotoId = "photoId"
  
  const val verifyTokenParamName = "verification-token"
}



fun Application.configureUserRoutes(){
  configureUserRouteCreate()
  configureUserRouteUpdate()
  configureUserRouteEmailInitialVerify()
  configureUserRouteProfilePhoto()
  configureUserRoutesCurrent()
  
  
  
  fun mongo() = MongoDbService.client
  
  
  
  routing {
    
    
    
    get(UserRoutes.getById) {
      val userId = call.parameters["id"]!!
      val userUuid = userId.toUuid()
      
      val m = mongo()
      val nUserId = UserMongo::id.name
      val nUserPhotos = UserMongo::photos.name
      val nPhotoBinData = UserProfilePhotoMongo::binData.name
      
      val userById = m.db.coll<UserMongo>("users")
        .find(Filters.eq(nUserId, userUuid))
        .projection(Document("$nUserPhotos.$nPhotoBinData", false))
        .limit(1)
        .firstOrNull()
      
      userById ?: return@get call.respondNoUserById()
      
      return@get call.respond(object {
        val user = userById.convertToSend(call.request)
      })
    }
    
    
    
    
    
    
    
    
    
    
  }
  
}