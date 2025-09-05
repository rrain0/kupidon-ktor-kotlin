package com.rrain.kupidon.models.db

import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import io.ktor.http.*
import java.util.UUID




data class UserProfilePhotoMetadataM(
  var id: UUID,
  var index: Int,
  var name: String,
  var ext: String,
) {
  
  fun getUrl(
    userId: UUID,
    host: String,
    port: Int,
  ) = let {
    val path = ApiV1Routes.userProfilePhoto
    val userIdParam = ApiV1Routes.userProfilePhotoNameParams.userId
    val photoIdParam = ApiV1Routes.userProfilePhotoNameParams.photoId
    val photoId = it.id
    
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
      .apply { path(path, "$name $id.$ext") }
      .build().toString()
  }
  
  fun toApi(
    userId: UUID,
    host: String,
    port: Int,
  ) = (
    mutableMapOf<String, Any?>(
      "id" to id,
      "index" to index,
      "name" to name,
      "ext" to ext,
      "url" to getUrl(userId, host, port),
    )
  )
  
}
