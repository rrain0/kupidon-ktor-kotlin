package com.rrain.kupidon.route.routes.app.api.v1.`users-list`

import com.rrain.kupidon.route.routes.app.api.v1.ApiV1Routes
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.model.UserDataType
import com.rrain.kupidon.service.db.mongo.model.UserMongo
import com.rrain.kupidon.service.db.mongo.model.UserProfilePhotoMongo
import com.rrain.kupidon.service.db.mongo.mongo
import com.rrain.`util-ktor`.request.getHostPort
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.bson.Document




object UsersListRoutes {
  const val base = "${ApiV1Routes.base}/users-list"
  
  const val all = "${base}/all"
}



fun Application.configureUsersListRoutes() {
  
  
  routing {
    
    get(UsersListRoutes.all) {
      val m = mongo()
      val nUserPhotos = UserMongo::photos.name
      val nPhotoBinData = UserProfilePhotoMongo::binData.name
      
      val users = m.db.coll<UserMongo>("users")
        .find()
        .projection(Document("$nUserPhotos.$nPhotoBinData", false))
        .toList()
      
      call.respond(object {
        val items = run {
          val (host, port) = call.request.getHostPort()
          users.map { it.convertToSend(UserDataType.Other, host, port) }
        }
      })
    }
    
    
  }
  
}