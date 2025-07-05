package com.rrain.util.any

import com.rrain.util.bool.asBool


inline fun <reified T> Any?.cast(): T = this as T


// Maps value by block if it == null
// Analog for (value ?: defaultValue) but can be used without ()
inline fun <T : Any?> T.mapNull(block: () -> T & Any): T & Any {
  return this ?: block()
}

// Maps value by block if it casts to true
inline fun <T : Any?> T.mapTruly(block: (it: T) -> T): T {
  return if (this.asBool) block(this) else this
}

