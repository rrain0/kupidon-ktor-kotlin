package com.rrain.kupidon.service.db.mongo.model

import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import io.ktor.http.*
import java.util.UUID




data class UserProfilePhotoMetadataMongo(
  val id: UUID,
  val index: Int,
  val name: String,
  val mimeType: String,
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
