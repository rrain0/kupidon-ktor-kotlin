package com.rrain.kupidon.route.route.user

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.plugin.JacksonObjectMapper
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondInvalidBody
import com.rrain.kupidon.route.util.respondNoUserById
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMetadataMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMongo
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.util.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.conversions.Bson
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import java.util.UUID





@OptIn(ExperimentalEncodingApi::class)
fun Application.configureUserRouteUpdate() {
  
  fun mongo() = MongoDbService.client
  
  
  
  data class ReplacePhoto(
    val id: UUID,
    val index: Int,
  )
  data class AddPhoto(
    val index: Int,
    val name: String,
    val dataUrl: String,
  )
  data class PhotosUpdations (
    val remove: List<UUID>,
    val replace: List<ReplacePhoto>,
    val add: List<AddPhoto>,
  )
  
  
  data class PreparedPhotosUpdations(
    val remove: List<UUID>,
    val replace: List<ReplacePhoto>,
    val add: List<UserProfilePhotoMongo>,
  )
  
  
  class Update {
    var props: MutableMap<String, Any?> = mutableMapOf(
      "currentPwdHashed" to null,
      "newPwdHashed" to null,
    )
    
    var name: String by props
    var birthDate: LocalDate by props
    var gender: Gender by props
    var aboutMe: String by props
    
    var currentPwdHashed: String? by props
    var newPwdHashed: String? by props
    
    var photos: PreparedPhotosUpdations by props
  }
  
  
  
  
  routing {
  authenticate {
  put(UserRoutes.update) {
    val userId = call.principal<JWTPrincipal>()!!.subject!!
    
    val data =
      try { call.receive<JsonNode>() }
      catch (ex: Exception){
        return@put call.respondInvalidBody()
      }
    
    if (!data.isObject){
      return@put call.respondInvalidBody(
        "Body must be json object"
      )
    }
    /*run {
      val allowedFields = setOf("name", "birthDate","gender","aboutMe","currentPwd","pwd","photos")
      if ((data.fieldNames().asSequence().toSet() - allowedFields).isNotEmpty()){
        return@put call.respondInvalidBody(
          "Allowed fileds: $allowedFields"
        )
      }
    }*/
    
    val update = Update()
    
    
    data.fields().forEach { (k,v) -> when(k){
        "name" -> {
          try {
            if (!v.isTextual) throw RuntimeException()
            val name = v.asText()
            if (name.isEmpty()) throw RuntimeException()
            if (name.length>100) throw RuntimeException()
            update.name = name
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
            update.birthDate = birthDate.toLocalDate()
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
            update.gender = gender
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
            update.aboutMe = aboutMe
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
            if (currentPwd.length !in 1..200) throw RuntimeException()
            currentPwd = currentPwd.let(PwdHashing::generateHash)
            update.currentPwdHashed = currentPwd
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
            if (pwd.length !in 6..200) throw RuntimeException()
            pwd = pwd.let(PwdHashing::generateHash)
            update.newPwdHashed = pwd
          }
          catch (ex: Exception) {
            return@put call.respondInvalidBody(
              "Password must be string and its length must be from 6 to 200 chars"
            )
          }
        }
        
        "photos" -> {
          val photosUpdations =
            try { JacksonObjectMapper.treeToValue<PhotosUpdations>(v) }
            catch (ex: Exception){
              ex.printStackTrace()
              println(ex.message)
              return@put call.respondInvalidBody(
                "Invalid photos format: ${ex.message}"
              )
            }
          
          photosUpdations.replace.forEachIndexed { i,it ->
            if (it.index !in 0..5) return@put call.respondInvalidBody(
              "photos.replace[$i].index must be in range ${0..5}"
            )
          }
          val preparedAdd = photosUpdations.add.mapIndexed { i,it ->
            if (it.index !in 0..5) return@put call.respondInvalidBody(
              "photos.add[$i].index must be in range ${0..5}"
            )
            if (it.name.length > 256) return@put call.respondInvalidBody(
              "photos.add[$i].name max length must be 256 chars"
            )
            println("name: ${it.name}")
            println("index: ${it.index}")
            println("dataUrl: ${it.dataUrl.substring(0,2000)}")
            val dataUrl =
              try { DataUrl(it.dataUrl) }
              catch (ex: Exception){
                return@put call.respondInvalidBody(
                  "photos.add[$i].dataUrl has invalid format: ${ex.message}"
                )
              }
            if (!dataUrl.mimeType.startsWith("image/")) return@put call.respondInvalidBody(
              "photos.add[$i].dataUrl must have mime-type starting with 'image/', " +
                "but yours is '${dataUrl.mimeType}'"
            )
            /*if (dataUrl.mimeType!="image/webp") return@put call.respondInvalidBody(
              "photos.add[$i].dataUrl must have 'image/webp' mime-type"
            )*/
            if (!dataUrl.isBase64) return@put call.respondInvalidBody(
              "photos.add[$i].dataUrl data must be base64 encoded"
            )
            val dataBytes = Base64.decode(dataUrl.data)
            println("size: ${dataBytes.size}")
            if (dataBytes.size > 0.4*1024*1024) return@put call.respondInvalidBody(
              "photos.add[$i].dataUrl data max size must be 0.4MB, " +
                "but yours is ${dataBytes.size} bytes"
            )
            UserProfilePhotoMongo(
              id = UUID.randomUUID(),
              index = it.index,
              name = it.name,
              mimeType = dataUrl.mimeType,
              binData = dataBytes,
            )
          }
          
          update.photos = PreparedPhotosUpdations(
            remove = photosUpdations.remove,
            replace = photosUpdations.replace,
            add = preparedAdd,
          )
        }
        
        else -> {
          return@put call.respondInvalidBody(
            "Unknown property '$k'"
          )
        }
      } }
    
    
    
    
    
    
    val m = mongo()
    val user = m.useTransaction { session ->
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
      val nPhotoName = UserProfilePhotoMetadataMongo::name.name
      val nPhotoMimeType = UserProfilePhotoMetadataMongo::mimeType.name
      val nPhotoBinData = UserProfilePhotoMongo::binData.name
      
      
      val userUuid = userId.toUuid()
      val userById = m.db.coll<UserMongo>("users")
        .find(session, Filters.eq(nUserId, userUuid))
        .projection(Document("$nUserPhotos.$nPhotoBinData", false))
        .firstOrNull()
      
      if (userById==null) {
        session.abortTransaction()
        return@put call.respondNoUserById()
      }
      
      
      /*m.db.coll<UserMongo>("users")
        .updateOne(session,
          Filters.eq(nUserId, userUuid),
          Updates.set("id", "4f699e2d-a492-40de-a54f-ed05c42203a4".toUuid())
        )
      
      println("after duplication key:")*/
      
      
      if (update.newPwdHashed!=null) {
        if (userById.pwd!=update.currentPwdHashed) {
          session.abortTransaction()
          return@put call.respondBadRequest(
            "INVALID_PWD",
            "Invalid current password"
          )
        }
        m.db.coll<UserMongo>("users")
          .updateOne(session,
            Filters.eq(nUserId, userUuid),
            buildList {
              add(Updates.set(nUserPwd, update.newPwdHashed))
              add(Updates.currentDate(nUserUpdated))
            }
            .let(Updates::combine)
          )
      }
      
      
      
      m.db.coll<UserMongo>("users")
        .updateOne(session,
          Filters.eq(nUserId, userUuid),
          buildList {
            if (update::name.name in update.props)
              add(Updates.set(nUserName, update.name))
            if (update::birthDate.name in update.props)
              add(Updates.set(nUserBirthDate, update.birthDate))
            if (update::gender.name in update.props)
              add(Updates.set(nUserGender, update.gender))
            if (update::aboutMe.name in update.props)
              add(Updates.set(nUserAboutMe, update.aboutMe))
            add(Updates.currentDate(nUserUpdated))
          }
          .let(Updates::combine)
        )
      
      
      
      // todo database is not checking distinction of 'index' field
      if (update::photos.name in update.props) {
        
        // remove photos by id
        m.db.coll<UserMongo>("users")
          .updateOne(session,
            Filters.eq(nUserId, userUuid),
            buildList {
              add(Updates.pull(
                nUserPhotos,
                Filters.`in`(nPhotoId, update.photos.remove)
              ))
              add(Updates.currentDate(nUserUpdated))
            }
            .let(Updates::combine)
          )
        
        // replace photos by id & new index
        update.photos.replace.forEach {
          m.db.coll<UserMongo>("users")
            .updateOne(session,
              Filters.eq(nUserId, userUuid),
              buildList {
                add(Updates.set(
                  "$nUserPhotos.$[i].$nPhotoIndex",
                  it.index
                ))
                add(Updates.currentDate(nUserUpdated))
              }
              .let(Updates::combine),
              //.also { println("updates: $it") },
              UpdateOptions().arrayFilters(listOf(
                Document("i.$nPhotoId", it.id)
              ))
            )
        }
        val replaceJson = """
          db.users.updateOne(
            { id: UUID("<user-uuid-as-string>") },
            { ${'$'}set: { "photos.${'$'}[i].index": <new-photo-index> } },
            { arrayFilters: [{ "i.id": UUID("<photo-uuid-as-string>") }] }
          )
        """.trimIndent()
        
        
        // add new photos by new data & new index
        m.db.coll<UserMongo>("users")
          .updateOne(session,
            Filters.eq(nUserId, userUuid),
            Updates.pushEach(nUserPhotos, update.photos.add)
          )
        
        // check index uniqueness
        /*val indices = m.db.coll<UserMongo>("users")
          .find(session,
            Filters.eq(nUserId, userUuid),
            
          )*/
      }
      
      
      
      m.db.coll<UserMongo>("users")
        .find(session, Filters.eq(UserMongo::id.name, userUuid))
        .projection(Document("$nUserPhotos.$nPhotoBinData", false))
        .first()
    }
    
    
    
    call.respond(object {
      val user = user.convertToSend(call.request)
    })
  } } }
}

