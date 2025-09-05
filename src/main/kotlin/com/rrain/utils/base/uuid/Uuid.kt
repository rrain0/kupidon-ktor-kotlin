package com.rrain.utils.base.uuid

import java.util.*



fun main() {
  generateAndPrintUuids()
}



fun generateAndPrintUuids() {
  repeat(16) { println(randomUuid()) }
}



fun randomUuid(): UUID = UUID.randomUUID()
fun String.toUuid(): UUID = UUID.fromString(this)

const val NilUuidStr = "00000000-0000-0000-0000-000000000000"
val NilUuid: UUID = NilUuidStr.toUuid()

val uuidAsStringComparator = Comparator<UUID> { a, b ->
  a.toString().compareTo(b.toString())
}
