package com.rrain.util.uuid

import java.util.*





fun main() {
  
  // generate UUIDs
  println((1..16).map { randomUuid() })
  
  run {
    val uuid1 = "db071eb2-40bd-4809-a97f-41107e014d99".toUuid()
    val uuid2 = "2cdc3c19-d5bf-457f-876d-a3b623d6f1fc".toUuid()
    val uuid3 = "8cc0014a-de38-48c9-a620-505cdef5d592".toUuid()
    val uuid4 = "4dfa2ed1-42b2-47e4-a9a0-90aacc673678".toUuid()
    println()
    println(listOf(uuid1, uuid2, uuid3, uuid4, ).sorted())
    println(listOf(uuid1, uuid4, uuid2, uuid3, ).sorted())
    println(listOf(uuid3, uuid4, uuid1, uuid2, ).sorted())
    println()
    println(listOf(uuid1, uuid2, uuid3, uuid4, ).sortedWith(uuidComparator))
    println(listOf(uuid1, uuid4, uuid2, uuid3, ).sortedWith(uuidComparator))
    println(listOf(uuid3, uuid4, uuid1, uuid2, ).sortedWith(uuidComparator))
  }
  
}



fun randomUuid() = UUID.randomUUID()
fun String.toUuid(): UUID = UUID.fromString(this)

const val NilUuidStr = "00000000-0000-0000-0000-000000000000"
val NilUuid: UUID = NilUuidStr.toUuid()

val uuidComparator = Comparator<UUID> { a, b -> a.toString().compareTo(b.toString()) }