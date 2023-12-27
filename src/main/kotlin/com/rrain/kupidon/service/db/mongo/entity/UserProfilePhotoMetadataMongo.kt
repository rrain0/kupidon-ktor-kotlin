package com.rrain.kupidon.service.db.mongo.entity

import java.util.UUID




data class UserProfilePhotoMetadataMongo(
  val id: UUID,
  val index: Int,
  val name: String,
  val mimeType: String,
)
