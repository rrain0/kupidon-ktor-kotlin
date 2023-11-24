package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondInvalidInputBody
import com.rrain.kupidon.route.util.respondNoUser
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.util.toUuid
import com.rrain.kupidon.util.toZonedDateTime
import com.rrain.kupidon.util.zonedDateTimePattern
import com.rrain.kupidon.util.zonedNow
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.bson.Document
import java.time.temporal.ChronoUnit
import java.util.*


fun Application.configureUserRouteUpdate() {
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    
    
    authenticate {
      put(UserRoutes.update) {
        val userId = call.principal<JWTPrincipal>()!!.subject!!
        val userIdUuid = userId.toUuid()
        
        val receivedData = try {
          call.receive<MutableMap<String, Any?>>()
        }
        catch (ex: Exception) {
          return@put call.respondInvalidInputBody()
        }
        
        val userToUpdate = mutableMapOf<String,Any?>()
        var currentPwd: String? = null
        
        receivedData.forEach { (k,v) -> when(k){
          
          "name" -> {
            try {
              if (v !is String) throw RuntimeException()
              if (v.isEmpty()) throw RuntimeException()
              if (v.length>100) throw RuntimeException()
              userToUpdate[UserMongo::name.name] = v
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "Name must be string and must not be empty and name max length is 100"
              )
            }
          }
          
          "birthDate" -> {
            try {
              if (v !is String) throw RuntimeException()
              val birthDate = v.toZonedDateTime()
              val nowWithUserZone = zonedNow()
                .withZoneSameInstant(birthDate.zone)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
              if (ChronoUnit.YEARS.between(birthDate, nowWithUserZone)<18){
                return@put call.respondInvalidInputBody(
                  "You must be at least 18 years old"
                )
              }
              userToUpdate[UserMongo::birthDate.name] = birthDate.toLocalDate()
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "'birthDate' must be string '$zonedDateTimePattern'" +
                  ", for example '2005-11-10T00:00:00.000+08:00'"
              )
            }
          }
          
          "gender" -> {
            try {
              if (v !is String) throw RuntimeException()
              val gender = Gender.valueOf(v)
              userToUpdate[UserMongo::gender.name] = gender
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "Gender must be string of 'MALE' | 'FEMALE'"
              )
            }
          }
          
          "aboutMe" -> {
            try {
              if (v !is String) throw RuntimeException()
              if (v.length>2000) throw RuntimeException()
              userToUpdate[UserMongo::aboutMe.name] = v
            } catch (ex: Exception){
              return@put call.respondInvalidInputBody(
                "'About me' must be string and must have max 2000 chars"
              )
            }
          }
          
          "currentPwd" -> {
            try{
              if (v !is String) throw RuntimeException()
              if (v.length<1 || v.length>200) throw RuntimeException()
              currentPwd = v.let(PwdHashing::generateHash)
            } catch (ex: Exception) {
              return@put call.respondInvalidInputBody(
                "Current password must be string and its length must be from 1 to 200 chars"
              )
            }
          }
          
          "pwd" -> {
            try{
              if (v !is String) throw RuntimeException()
              if (v.length<6 || v.length>200) throw RuntimeException()
              userToUpdate[UserMongo::pwd.name] = v.let(PwdHashing::generateHash)
            } catch (ex: Exception) {
              return@put call.respondInvalidInputBody(
                "Password must be string and its length must be from 6 to 200 chars"
              )
            }
          }
          
          else -> {
            return@put call.respondInvalidInputBody(
              "Unknown property '$k'"
            )
          }
        }}
        
        
        val m = mongo()
        val user = m.useTransaction { session ->
          val userById = m.db.coll<UserMongo>("users")
            .find(session, Filters.eq(UserMongo::id.name, userIdUuid))
            .toList().firstOrNull()
          
          userById ?: return@put call.respondNoUser()
          
          if ("pwd" in userToUpdate && userById.pwd!=currentPwd)
            return@put call.respondBadRequest(
              "INVALID_PWD",
              "Invalid current password"
            )
          
          
          m.db.coll<UserMongo>("users")
            .updateOne(session,
              Filters.eq(UserMongo::id.name, userIdUuid),
              userToUpdate.map { (k,v)->Updates.set(k,v) }.let { Updates.combine(it) }
            )
          
          m.db.coll<UserMongo>("users")
            .find(session, Filters.eq(UserMongo::id.name, userIdUuid))
            .toList().first()
        }
        
        
        
        call.respond(object {
          val user = user.toMapToSend()
        })
      }
    }
    
    
    
    
  }
}