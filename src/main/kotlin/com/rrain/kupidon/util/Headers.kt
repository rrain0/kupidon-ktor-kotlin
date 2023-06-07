package com.rrain.kupidon.util

import io.ktor.server.application.*
import io.ktor.util.*


fun ApplicationCall.printHeaders(): Map<String,List<String>> {
  val headersMap = this.request.headers.toMap()
  println("request headers: {")
  headersMap.forEach {(k,v)->println("  $k = $v") }
  println("}")
  return headersMap
}