package com.rrain.kupidon.route.util



enum class RespErrorCode(val msg: String) {
  NO_USER("No user with such id"),
  INVALID_INPUT_BODY("Invalid request body format"),
}