package com.rrain.kupidon.route.route.user

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.plugin.JacksonObjectMapper
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondInvalidBody
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
import java.time.temporal.ChronoUnit



fun Application.configureUserRouteUpdate() {
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    
    data class Photo(
      val id: String?,
      val data: String?,
    )
    authenticate {
      put(UserRoutes.update) {
        val userId = call.principal<JWTPrincipal>()!!.subject!!
        val userIdUuid = userId.toUuid()
        
        val data =
        try { call.receive<JsonNode>() }
        catch (ex: Exception){
          return@put call.respondInvalidBody()
        }
        
        val acceptedProps = listOf(
          "name", "birthDate", "gender", "aboutMe",
          "currentPwd", "pwd", "photos",
        )
        var dataToUpdate = data.fields().asSequence().toList()
          .associateTo(mutableMapOf<String,Any?>()) { (k,v) -> when(k){
            "name" -> {
              try {
                if (!v.isTextual) throw RuntimeException()
                val name = v.asText()
                if (name.isEmpty()) throw RuntimeException()
                if (name.length>100) throw RuntimeException()
                "name" to name
              }
              catch (ex: Exception){
                return@put call.respondInvalidBody(
                  "Name must be string and must not be empty and name max length is 100"
                )
              }
            }
            
            "birthDate" -> {
              try {
                if (!v.isTextual) throw RuntimeException()
                val birthDate = v.asText().toZonedDateTime()
                val nowWithUserZone = zonedNow()
                  .withZoneSameInstant(birthDate.zone)
                  .withHour(0)
                  .withMinute(0)
                  .withSecond(0)
                  .withNano(0)
                if (ChronoUnit.YEARS.between(birthDate, nowWithUserZone)<18){
                  return@put call.respondInvalidBody(
                    "You must be at least 18 years old"
                  )
                }
                "birthDate" to birthDate.toLocalDate()
              }
              catch (ex: Exception){
                return@put call.respondInvalidBody(
                  "'birthDate' must be string '$zonedDateTimePattern'" +
                    ", for example '2005-11-10T00:00:00.000+08:00'"
                )
              }
            }
            
            "gender" -> {
              try {
                val gender = JacksonObjectMapper.treeToValue<Gender>(v)
                "gender" to gender
              }
              catch (ex: Exception){
                return@put call.respondInvalidBody(
                  "Gender must be string of ${Gender.entries}"
                )
              }
            }
            
            "aboutMe" -> {
              try {
                if (!v.isTextual) throw RuntimeException()
                val aboutMe = v.asText()
                if (aboutMe.length>2000) throw RuntimeException()
                "aboutMe" to aboutMe
              }
              catch (ex: Exception){
                return@put call.respondInvalidBody(
                  "'About me' must be string and must have max 2000 chars"
                )
              }
            }
            
            "currentPwd" -> {
              try {
                if (!v.isTextual) throw RuntimeException()
                var currentPwd = v.asText()
                if (currentPwd.length<1 || currentPwd.length>200) throw RuntimeException()
                currentPwd = currentPwd.let(PwdHashing::generateHash)
                "currentPwd" to currentPwd
              }
              catch (ex: Exception) {
                return@put call.respondInvalidBody(
                  "Current password must be string and its length must be from 1 to 200 chars"
                )
              }
            }
            
            "pwd" -> {
              try {
                if (!v.isTextual) throw RuntimeException()
                var pwd = v.asText()
                if (pwd.length<6 || pwd.length>200) throw RuntimeException()
                pwd = pwd.let(PwdHashing::generateHash)
                "pwd" to pwd
              }
              catch (ex: Exception) {
                return@put call.respondInvalidBody(
                  "Password must be string and its length must be from 6 to 200 chars"
                )
              }
            }
            
            "photos" -> {
              try {
                val photos = JacksonObjectMapper.treeToValue<List<Photo?>>(v)
                
                //println("photos: $photos")
                //println("photos[1]?.id==null: ${photos[1]?.id==null}")
                // todo convert data url to ByteArray
                // todo name, mimeType, check size
                // todo if it has id (only id) so it is from server otherwise it is new photo
                "photos" to photos
              }
              catch (ex: Exception) {
                return@put call.respondInvalidBody(
                  "Invalid photos format"
                )
              }
            }
            
            else -> {
              return@put call.respondInvalidBody(
                "Unknown property '$k'"
              )
            }
          } }
        
        
        
        
        
        var currentPwd: String? = null
        if ("currentPwd" in dataToUpdate) currentPwd = dataToUpdate["currentPwd"] as String
        dataToUpdate.remove("currentPwd")
        dataToUpdate.remove("photos")
        val userToUpdate: MutableMap<String,Any?> = dataToUpdate
        
        
        
        
        
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
              userToUpdate
                .map { (k,v)->Updates.set(k,v) }
                .toMutableList()
                .apply { add(Updates.currentDate(UserMongo::updated.name)) }
                .let { Updates.combine(it) }
            )
          
          m.db.coll<UserMongo>("users")
            .find(session, Filters.eq(UserMongo::id.name, userIdUuid))
            .toList().first()
        }
        
        
        
        call.respond(object {
          val user = user.convertToSend(call.request)
        })
      }
    }
    
    
    
    
  }
}