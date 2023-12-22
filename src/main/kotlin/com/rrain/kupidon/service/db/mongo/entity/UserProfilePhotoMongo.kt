package com.rrain.kupidon.service.db.mongo.entity

import java.util.UUID




data class UserProfilePhotoMongo(
  val id: UUID,
  val name: String,
  val mimeType: String,
  val binData: ByteArray,
)
