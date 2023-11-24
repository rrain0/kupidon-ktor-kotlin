package com.rrain.kupidon.route.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*



suspend inline fun ApplicationCall.respondBadRequest
(code: String, msg: String)
= this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = code
    val msg = msg
  }
)


// Пользователь не найден
suspend inline fun ApplicationCall.respondNoUser() = this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = RespErrorCode.NO_USER.name
    val msg = RespErrorCode.NO_USER.msg
  }
)

// Некорректный формат тела запроса
suspend inline fun ApplicationCall.respondInvalidInputBody(msg: String? = null) = this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = RespErrorCode.INVALID_INPUT_BODY.name
    val msg = msg ?: RespErrorCode.INVALID_INPUT_BODY.msg
  }
)