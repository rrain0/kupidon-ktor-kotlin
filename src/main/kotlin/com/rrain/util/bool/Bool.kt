package com.rrain.util.bool



fun bool(value: Any?): Boolean {
  if (value == null) return false
  if (value == "") return false
  if (value == false) return false
  if (value == 0) return false
  if (value == 0L) return false
  if (value == 0.0) return false
  if (value == 0f) return false
  return true
}
fun Any?.toBool() = bool(this)
val Any?.bool get() = bool(this)


