package com.rrain.kupidon.model.db

import org.bson.types.Binary
import java.util.UUID




data class UserProfilePhotoM(
  var id: UUID,
  var index: Int,
  var name: String,
  var mimeType: String,
  var binData: Binary,
)
