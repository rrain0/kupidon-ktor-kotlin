package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.util.respondNoUserById
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMongo
import com.rrain.kupidon.util.toUuid
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document




fun Application.configureUserRoutesCurrent(){
  
  fun mongo() = MongoDbService.client
  
  
  
  routing {
    
    
    
    authenticate {
      get(UserRoutes.current) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
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
        
        call.respond(object {
          val user = userById.convertToSend(call.request)
        })
      }
    }
    
    
    
    
  }
  
}