package com.rrain.utils.base.bool



fun bool(value: Any?): Boolean = when (value) {
  null, "", false, 0, 0L, 0.0, 0f -> false
  else -> true
}
fun Any?.toBool() = bool(this)
val Any?.bool get() = bool(this)


