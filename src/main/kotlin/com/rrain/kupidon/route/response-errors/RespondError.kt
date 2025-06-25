package com.rrain.kupidon.route.`response-errors`

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
) = respond(
  HttpStatusCode.BadRequest,
  CodeMsg(code, msg)
)
suspend inline fun ApplicationCall.respondBadRequest(
  codeMsg: CodeMsg
) = respond(
  HttpStatusCode.BadRequest,
  codeMsg,
)




// Запрос сформирован правильно, но ресурса не существует.
// Например если не существует чего-то по id.
// У ошибки 404 не может быть тела.
suspend inline fun ApplicationCall.respondNotFound() = respond(
  HttpStatusCode.NotFound
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