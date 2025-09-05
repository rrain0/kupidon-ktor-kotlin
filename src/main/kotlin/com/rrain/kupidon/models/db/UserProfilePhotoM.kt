package com.rrain.kupidon.models.db

import org.bson.types.Binary
import java.util.UUID




data class UserProfilePhotoM(
  var id: UUID,
  var index: Int,
  var name: String,
  var ext: String,
  var binData: Binary,
)
