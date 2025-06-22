package com.rrain.kupidon.route.`response-errors`

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*


/*
  {
    code: ... for machines,
    msg: ... for humans,
    ... additional fields for machines if necessary,
    extraCode?: ... for machines,
  }

 */


suspend inline fun ApplicationCall.respondBadRequest(
  code: String, msg: String,
) = this.respond(
  HttpStatusCode.BadRequest,
  mapOf(
    "code" to code,
    "msg" to msg,
  ),
)
suspend inline fun ApplicationCall.respondBadRequest(body: Any) = this.respond(
  HttpStatusCode.BadRequest,
  body,
)




suspend inline fun ApplicationCall.respondNotFound(msg: String) = (
  respondNotFound("NOT_FOUND", msg)
)
suspend inline fun ApplicationCall.respondNotFound(
  code: String, msg: String,
) = this.respond(
  HttpStatusCode.NotFound,
  mapOf(
    "code" to code,
    "msg" to msg,
  ),
)



// Некорректный формат тела запроса
suspend inline fun ApplicationCall.respondInvalidBody(
  msg: String = "Invalid request body format",
) = this.respond(
  HttpStatusCode.BadRequest,
  mapOf(
    "code" to "INVALID_INPUT_BODY",
    "msg" to msg,
  ),
)

// Некорректный формат параметров запроса (query params)
suspend inline fun ApplicationCall.respondInvalidParams(
  msg: String = "Invalid request params format",
) = this.respond(
  HttpStatusCode.BadRequest,
  mapOf(
    "code" to "INVALID_PARAMS",
    "msg" to msg,
  ),
)




// TODO refactor
// Пользователь не найден
suspend inline fun ApplicationCall.respondNoUserById() = this.respond(
  HttpStatusCode.BadRequest,
  mapOf(
    "code" to "NO_USER",
    "msg" to "No user with such id",
  ),
)