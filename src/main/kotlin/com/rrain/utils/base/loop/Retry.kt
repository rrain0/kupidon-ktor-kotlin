package com.rrain.utils.base.loop



fun <T> retryIf(
  producer: () -> T,
  checker: (T) -> Boolean,
  orElse: () -> T = { throw IllegalStateException() },
  maxRetries: Int? = 100,
): T {
  var i = 0
  while (maxRetries == null || i <= maxRetries) {
    val v = producer()
    if (!checker(v)) return v
    i++
  }
  return orElse()
}



fun <T> retryUntil(
  producer: () -> T,
  checker: (T) -> Boolean,
  orElse: () -> T = { throw IllegalStateException() },
  maxRetries: Int? = 100,
): T = retryIf(
  producer,
  { !checker(it) },
  orElse,
  maxRetries,
)