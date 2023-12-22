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



suspend inline fun ApplicationCall.respondBadRequest
(body: Any)
= this.respond(
  HttpStatusCode.BadRequest,
  body
)





// Пользователь не найден
suspend inline fun ApplicationCall.respondNoUser() = this.respond(
  HttpStatusCode.BadRequest,
  ErrNoUserById,
)

// Некорректный формат тела запроса
suspend inline fun ApplicationCall.respondInvalidBody(msg: String? = null) = this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = ErrInvalidBody.code
    val msg = msg ?: ErrInvalidBody.msg
  }
)

// Некорректный формат параметров запроса
suspend inline fun ApplicationCall.respondInvalidParams(msg: String? = null) = this.respond(
  HttpStatusCode.BadRequest,
  object {
    val code = ErrInvalidParams.code
    val msg = msg ?: ErrInvalidParams.msg
  }
)