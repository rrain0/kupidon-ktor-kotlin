package com.rrain.utils.base.any

import com.rrain.utils.base.bool.bool



// value.cast<T>() -> (value as T)
inline fun <reified T> Any?.cast(): T = this as T


// Maps value by block if value is null.
// value.ifNull { defaultValue } -> (value ?: defaultValue)
// I want to write fun <T, R> T.ifNull(block: () -> R): (T & Any) | R but it is impossible.
fun <T> T.ifNull(block: () -> T & Any): T & Any {
  return this ?: block()
}
// Platform declaration clash: The following declarations have the same JVM signature
// fun <T, R> T.ifNull(block: () -> R): R where T : R {
//   return this ?: block()
// }

// value.ifNotNull { } -> value?.let { }

// Maps value by block if it coerces to true
fun <T, R> T.ifTruly(block: (it: T) -> R): R where T : R {
  if (this.bool) return block(this)
  return this
}

// Maps value by block if it coerces to false
fun <T, R> T.ifFalsy(block: (it: T) -> R): R where T : R {
  if (!this.bool) return block(this)
  return this
}
