package com.rrain.kupidon.route.util





object ErrNoUserById {
  val code = "NO_USER"
  val msg = "No user with such id"
}
object ErrInvalidBody {
  val code = "INVALID_INPUT_BODY"
  val msg = "Invalid request body format"
}
object ErrInvalidParams {
  val code = "INVALID_PARAMS"
  val msg = "Invalid request params format"
}