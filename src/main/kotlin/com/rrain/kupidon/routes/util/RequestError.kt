package com.rrain.kupidon.routes.util



enum class RequestError(val msg: String) {
  NO_USER("No user with such id"),
  INVALID_INPUT_BODY("Invalid request body format"),
}