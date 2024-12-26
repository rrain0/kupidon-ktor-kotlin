package com.rrain.kupidon.util.application

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*


operator fun ApplicationConfig.get(prop: String) = this.property(prop).getString()



fun ApplicationCall.printHeaders(): Map<String,List<String>> {
  val headersMap = this.request.headers.toMap()
  println("request headers: {")
  headersMap.forEach {(k,v)-> println("  $k = $v") }
  println("}")
  return headersMap
}

