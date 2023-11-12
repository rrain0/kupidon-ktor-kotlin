package com.rrain.kupidon.util.extension

import com.rrain.kupidon.routes.util.RequestError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*



// Пользователь не найден
suspend inline fun ApplicationCall.respondNoUser() = this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = RequestError.NO_USER.name
    val msg = RequestError.NO_USER.msg
  }
)

// Некорректный формат тела запроса
suspend inline fun ApplicationCall.respondInvalidInputBody(msg: String? = null) = this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = RequestError.INVALID_INPUT_BODY.name
    val msg = msg ?: RequestError.INVALID_INPUT_BODY.msg
  }
)