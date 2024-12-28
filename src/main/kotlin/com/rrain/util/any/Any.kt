package com.rrain.util.any

import com.rrain.util.bool.bool


inline fun <reified T> Any?.cast(): T = this as T


// Maps value by block if it == null
// Analog for (value ?: defaultValue)
fun <T : Any?>T.mapNull(block: () -> T & Any): T & Any {
  if (this == null) return block()
  return this
}

// Maps value by block if it casts to true
fun <T : Any?>T.mapTruly(block: (it: T) -> T): T {
  if (this.bool) return block(this)
  return this
}

