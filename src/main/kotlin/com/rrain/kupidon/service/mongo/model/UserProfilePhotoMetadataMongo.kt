package com.rrain.kupidon.service.mongo.model

import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import io.ktor.http.*
import java.util.UUID
import kotlin.text.get




data class UserProfilePhotoMetadataMongo(
  var id: UUID,
  var index: Int,
  var name: String,
  var mimeType: String,
) {
  
  fun toApi(
    userId: UUID,
    host: String,
    port: Int,
  ) = this.let {
    mutableMapOf<String, Any?>(
      "id" to id,
      "index" to index,
      "name" to name,
      "mimeType" to mimeType,
      "url" to run {
        val path = ApiV1Routes.userProfilePhoto
        val userIdParam = ApiV1Routes.userProfilePhotoNameParams.userId
        val photoIdParam = ApiV1Routes.userProfilePhotoNameParams.photoId
        val photoId = it.id
        // TODO save extension in db instead of mime type (maxLen = 20)
        val extension = Regex("""[^/]+/(?<ext>[^/]+)""")
          .matchEntire(mimeType)
          ?.let { it.groups["ext"]?.value }
        val extPart = extension?.let { ".$it" } ?: ""
        
        URLBuilder(
          protocol = URLProtocol.HTTPS,
          host = host,
          port = port,
          //pathSegments = listOf(name),
          parameters = Parameters.build {
            append(userIdParam, userId.toString())
            append(photoIdParam, photoId.toString())
          }
        )
          .apply { path(path, "$name $id$extPart") }
          .build().toString()
      },
    )
  }
  
}
