package com.rrain.kupidon.service.mongo.model

import org.bson.types.Binary
import java.util.UUID




data class UserProfilePhotoMongo(
  var id: UUID,
  var index: Int,
  var name: String,
  var mimeType: String,
  var binData: Binary,
)
