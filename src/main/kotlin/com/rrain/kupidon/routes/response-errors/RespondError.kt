package com.rrain.kupidon.routes.`response-errors`

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*



/*
  {
    code: ... for machines,
    message: ... for humans,
  }
 */


suspend inline fun ApplicationCall.respondBadRequest(
  code: String,
  msg: String = ""
) = (
  respondBadRequest(CodeMsg(code, msg))
)
suspend inline fun ApplicationCall.respondBadRequest(
  codeMsg: CodeMsg
) = respond(
  HttpStatusCode.BadRequest,
  codeMsg,
)




suspend inline fun ApplicationCall.respond401Unauthorized(
  code: String,
  msg: String = ""
) = (
  respond401Unauthorized(CodeMsg(code, msg))
)
suspend inline fun ApplicationCall.respond401Unauthorized(
  codeMsg: CodeMsg
) = respond(
  HttpStatusCode.Unauthorized,
  codeMsg,
)



// Запрос сформирован правильно, но ресурса не существует.
// Например если не существует чего-то по id.
suspend inline fun ApplicationCall.respondNotFound(
  code: String, msg: String = ""
) = (
  respondNotFound(CodeMsg(code, msg))
)
suspend inline fun ApplicationCall.respondNotFound(
  codeMsg: CodeMsg
) = respond(
  HttpStatusCode.NotFound,
  codeMsg,
)




// Некорректный формат запроса (хоть в path params, хоть в query params, хоть в body)
suspend inline fun ApplicationCall.respondInvalidRequest(
  msg: String = "Invalid request format: invalid path param format or invalid query param format or invalid body format",
) = this.respond(
  HttpStatusCode.BadRequest,
  CodeMsg("INVALID_REQUEST_FORMAT", msg)
)




// Некорректный формат тела запроса
suspend inline fun ApplicationCall.respondInvalidBody(
  msg: String = "Invalid request body format",
) = this.respond(
  HttpStatusCode.BadRequest,
  CodeMsg("INVALID_INPUT_BODY", msg)
)

// TODO а оно мне вообще надо?
// Некорректный формат параметров запроса (query params)
suspend inline fun ApplicationCall.respondInvalidParams(
  msg: String = "Invalid request params format",
) = this.respond(
  HttpStatusCode.BadRequest,
  CodeMsg("INVALID_PARAMS", msg)
)




// TODO refactor to not found code
// Пользователь не найден
suspend inline fun ApplicationCall.respondNoUserById() = respond(
  HttpStatusCode.BadRequest,
  CodeMsg("NO_USER", "No user with such id")
)