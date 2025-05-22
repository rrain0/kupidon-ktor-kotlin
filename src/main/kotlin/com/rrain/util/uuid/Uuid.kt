package com.rrain.util.uuid

import java.util.*


fun main() {
  
  // generate UUIDs
  println((1..16).map { UUID.randomUUID() })
  
}

fun String.toUuid(): UUID = UUID.fromString(this)


const val NilUuidStr = "00000000-0000-0000-0000-000000000000"
val NilUuid: UUID = NilUuidStr.toUuid()