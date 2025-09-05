package com.rrain.kupidon.routes.`convert-or-error`

import com.rrain.kupidon.routes.`response-errors`.respondInvalidRequest
import com.rrain.utils.base.uuid.toUuid
import io.ktor.server.routing.RoutingContext
import java.util.UUID



context(routingContext: RoutingContext)
suspend fun String?.toUuidOr400(): UUID {
  return try {
    this!!.toUuid()
  }
  catch (ex: Exception) {
    routingContext.call.respondInvalidRequest()
    throw ex
  }
}
