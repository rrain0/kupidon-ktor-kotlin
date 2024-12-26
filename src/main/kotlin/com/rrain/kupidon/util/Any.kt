package com.rrain.kupidon.util.Any




inline fun <reified T> Any?.cast(): T = this as T


// better use (value ?: defaultValue)
fun <T : Any?>T.mapNull(block: () -> T & Any): T & Any {
  if (this == null) return block()
  return this
}

