package com.rrain.kupidon.entity.ARCHIVE.db

import java.util.UUID


class RoleDb(
  var map: MutableMap<String,Any?> = mutableMapOf(
    "id" to null,
    "role" to null,
  )
) {
  var id: UUID by map
  var role: String by map
}