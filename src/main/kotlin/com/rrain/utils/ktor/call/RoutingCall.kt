package com.rrain.utils.ktor.call

import io.ktor.http.Parameters
import io.ktor.server.routing.RoutingCall



val RoutingCall.pathParams get() = request.pathVariables
val RoutingCall.queryParams get() = request.queryParameters

class BoolParametersAccess(var parameters: Parameters) {
  operator fun get(name: String): Boolean {
    val value = parameters[name]
    // Для ktor "?boolParam" и "?boolParam=" - одно и тоже - значение ""
    return value != null && value != "false"
  }
}

val RoutingCall.boolQueryParams get() = BoolParametersAccess(request.queryParameters)
