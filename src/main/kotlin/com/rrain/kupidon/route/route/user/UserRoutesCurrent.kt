package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.route.util.respondNoUser
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.util.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList





fun Application.configureUserRoutesCurrent(){
  
  fun mongo() = MongoDbService.client
  
  
  
  routing {
    
    
    
    authenticate {
      get(UserRoutes.current) {
        val principal = call.principal<JWTPrincipal>()!!
        val userId = principal.subject!!
        val userById = mongo().db.coll<UserMongo>("users")
          .find(Filters.eq(UserMongo::id.name, userId.toUuid()))
          .firstOrNull()
        userById ?: return@get call.respondNoUser()
        call.respond(object {
          val user = userById.convertToSend(call.request)
        })
      }
    }
    
    
    
    
  }
  
}