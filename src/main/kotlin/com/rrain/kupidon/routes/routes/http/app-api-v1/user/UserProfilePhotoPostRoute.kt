package com.rrain.kupidon.routes.routes.http.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.rrain.kupidon.plugins.authUserId
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.`response-errors`.respondNoUserById
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.mongo.UpdatesUpdatedAt
import com.rrain.kupidon.services.mongo.collUsers
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.UserProfilePhotoM
import com.rrain.kupidon.models.db.projectionUserM
import com.rrain.kupidon.services.mongo.useSingleDocTx
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.base.uuid.toUuid
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
        var ext: String? = null,
        var binData: ByteArray? = null,
      )
      
      post(ApiV1Routes.userProfilePhoto) {
        val userUuid = authUserId
        
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
                "ext" -> {
                  if (it is PartData.FormItem) ext = it.value
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
          UserProfilePhotoM(
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
                  "Photo name max length is 256 chars"
                )
              },
            ext
              .let {
                it ?: return@post call.respondInvalidBody(
                  "Field 'ext' must exist and must be String"
                )
              }
              .also {
                if (it.length > 20) return@post call.respondInvalidBody(
                  "Photo filename extension max length is 20 chars"
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
              Filters.eq(UserM::id.name, userUuid),
              listOf(UpdatesUpdatedAt(UserM::updatedAt.name, now)),
              FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                .projection(projectionUserM),
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
            Filters.eq(UserM::id.name, userUuid),
            Updates.pushEach(UserM::photos.name, listOf(photo)),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
              .projection(projectionUserM),
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