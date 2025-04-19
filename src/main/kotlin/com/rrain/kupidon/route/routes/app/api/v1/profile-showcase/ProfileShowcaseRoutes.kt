package com.rrain.kupidon.route.routes.app.api.v1.`profile-showcase`

import com.rrain.kupidon.route.routes.app.api.v1.ApiV1Routes
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMongo
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.bson.Document




object ProfileShowcaseRoutes {
  const val base = "${ApiV1Routes.base}/profile-showcase"
  
  const val listAll = "${base}/list-all"
}



fun Application.configureProfileShowcaseRoutes() {
  
  fun mongo() = MongoDbService.client
  
  routing {
    
    authenticate {
      get(ProfileShowcaseRoutes.listAll) {
        val m = mongo()
        val nUserPhotos = UserMongo::photos.name
        val nPhotoBinData = UserProfilePhotoMongo::binData.name
        
        val users = m.db.coll<UserMongo>("users")
          .find()
          .projection(Document("$nUserPhotos.$nPhotoBinData", false))
          .toList()
        
        call.respond(object {
          val items = users.map { it. convertToSend(call.request) }
        })
      }
    }
    
    
  }
  
}