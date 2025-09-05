package com.rrain.utils.ktor.call

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.util.toMap
import kotlin.collections.component1
import kotlin.collections.component2



// Returns host (domain or ip) by which client requested the server
val ApplicationCall.host get() = this.request.origin.serverHost
// Returns port by which client requested the server
val ApplicationCall.port get() = this.request.origin.serverPort



fun ApplicationCall.printHeaders(): Map<String,List<String>> {
  val headersMap = this.request.headers.toMap()
  println("request headers: {")
  headersMap.forEach { (k, v) -> println("  $k = $v") }
  println("}")
  return headersMap
}
