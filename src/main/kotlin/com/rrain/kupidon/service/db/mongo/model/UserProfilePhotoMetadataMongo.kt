package com.rrain.kupidon.service.db.mongo.model

import com.rrain.kupidon.route.route.user.UserRoutes
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import java.util.UUID




data class UserProfilePhotoMetadataMongo(
  val id: UUID,
  val index: Int,
  val name: String,
  val mimeType: String,
) {
  fun convertToSend(userUuid: UUID, request: ApplicationRequest) = this.let {
    object {
      val id = it.id
      val index = it.index
      val name = it.name
      val mimeType = it.mimeType
      var url = run {
        val host = request.origin.serverHost
        val port = request.origin.serverPort
        val path = UserRoutes.getProfilePhoto
        val userIdParam = UserRoutes.getProfilePhotoParamUserId
        val photoIdParam = UserRoutes.getProfilePhotoParamPhotoId
        val photoId = it.id
        val extension = Regex("""[^/]+/(?<ext>[^/]+)""")
          .matchEntire(mimeType)
          ?.let { it.groups["ext"]?.value }
          ?.let { "."+it }
          ?: ""
        URLBuilder(
          protocol = URLProtocol.HTTPS,
          host = host,
          port = port,
          //pathSegments = listOf(name),
          parameters = Parameters.build {
            append(userIdParam, userUuid.toString())
            append(photoIdParam, photoId.toString())
          }
        )
          .apply { path(path, "$name $id$extension") }
          .build().toString()
      }
    }
  }
}
