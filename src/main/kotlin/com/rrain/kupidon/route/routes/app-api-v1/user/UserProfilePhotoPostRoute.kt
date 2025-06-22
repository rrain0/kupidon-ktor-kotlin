package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.rrain.kupidon.plugin.authUserUuid
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.`response-errors`.respondNoUserById
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.UpdatesUpdatedAt
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.UserMongo
import com.rrain.kupidon.service.mongo.model.UserProfilePhotoMongo
import com.rrain.kupidon.service.mongo.model.projectionUserMongo
import com.rrain.kupidon.service.mongo.useSingleDocTx
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import com.rrain.util.`date-time`.now
import com.rrain.util.uuid.toUuid
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.bson.types.Binary




fun Application.addUserProfilePhotoPostRoute() {
  routing {
    authenticate {
      
      data class PartialPhotoBodyIn(
        var id: String? = null,
        var index: Int? = null,
        var name: String? = null,
        var mimeType: String? = null,
        var binData: ByteArray? = null,
      )
      
      post(ApiV1Routes.userProfilePhoto) {
        val userUuid = authUserUuid
        
        val partialPhoto = PartialPhotoBodyIn()
        val propsCnt = 5
        
        val multipart = call.receiveMultipart()
        partialPhoto.apply {
          for (i in propsCnt downTo 0) {
            val it = multipart.readPart() ?: break
            try {
              if (i == 0) {
                return@post call.respondInvalidBody("Object must have $propsCnt props")
              }
              val prop = it.name
              when (prop) {
                "id" -> {
                  if (it is PartData.FormItem) id = it.value
                }
                "index" -> {
                  if (it is PartData.FormItem) index = it.value.toIntOrNull()
                }
                "name" -> {
                  if (it is PartData.FormItem) name = it.value
                }
                "mimeType" -> {
                  if (it is PartData.FormItem) mimeType = it.value
                }
                "binData" -> {
                  if (it is PartData.FileItem) binData = it.provider().toByteArray()
                }
                else -> {
                  return@post call.respondInvalidBody("Unknown property '$prop'")
                }
              }
            }
            finally { it.dispose() }
          }
        }
        
        val photo = partialPhoto.run {
          UserProfilePhotoMongo(
            id
              .let {
                it ?: return@post call.respondInvalidBody(
                  "Field 'id' must exist and must be UUID-string"
                )
              }
            .let {
              try { it.toUuid() }
              catch (ex: Exception) {
                return@post call.respondInvalidBody("'id' must be UUID-string")
              }
            },
            index
              .let {
                it ?: return@post call.respondInvalidBody(
                  "Field 'index' must exist and must be Int"
                )
              }
              .also {
                if (it !in 0..5) return@post call.respondInvalidBody(
                  "Photo index must be in range ${0..5}"
                )
              },
            name
              .let {
                it ?: return@post call.respondInvalidBody(
                  "Field 'name' must exist and must be String"
                )
              }
              .also {
                if (it.length > 256) return@post call.respondInvalidBody(
                  "Photo name max length must be 256 chars"
                )
              },
            mimeType
              .let {
                it ?: return@post call.respondInvalidBody(
                  "Field 'mimeType' must exist and must be String"
                )
              }
              .also {
                if (!it.startsWith("image/")) return@post call.respondInvalidBody(
                  "Photo mime-type must start with 'image/', but yours is '$it'"
                )
              },
            binData
              .let {
                it ?: return@post call.respondInvalidBody(
                  "Field 'bytes' must exist and must be File"
                )
                Binary(it)
              }
              .also {
                if (it.data.size > 0.4 * 1024 * 1024) {
                  return@post call.respondInvalidBody(
                    "Photo bytes max size is 0.4MB, but yours is $it bytes"
                  )
                }
              },
          )
        }
        
        
        val updatedUser = useSingleDocTx { session, abort ->
          val now = now()
          val userById = collUsers
            .findOneAndUpdate(
              session,
              Filters.eq(UserMongo::id.name, userUuid),
              listOf(UpdatesUpdatedAt(UserMongo::updatedAt.name, now)),
              FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                .projection(projectionUserMongo),
            )
          
          if (userById == null) {
            abort()
            return@post call.respondNoUserById()
          }
          if (userById.photos.any { it.index == photo.index }) {
            abort()
            return@post call.respondInvalidBody("Duplicate photo index: ${photo.index}")
          }
          // TODO генерировать и возвращать новый id при коллизии
          if (userById.photos.any { it.id == photo.id }) {
            abort()
            return@post call.respondInvalidBody("Duplicate photo id in single user")
          }
          if (userById.photos.size >= 6) {
            abort()
            return@post call.respondInvalidBody("Maximum photos count is 6")
          }
          
          val updatedUser = collUsers.findOneAndUpdate(
            session,
            Filters.eq(UserMongo::id.name, userUuid),
            Updates.pushEach(UserMongo::photos.name, listOf(photo)),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
              .projection(projectionUserMongo),
          )!!
          
          updatedUser
        }
        
        // TODO возвращать только новую фотку или массив фоток
        call.respond(mapOf(
          "user" to updatedUser.toApi(UserDataType.Current, call.host, call.port),
        ))
      }
    }
  }
}