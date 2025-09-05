package com.rrain.utils.base.comparison



fun <T : Comparable<T>> maxOf(a: T?, b: T?): T? = when {
  b == null -> a
  a == null -> b
  a >= b -> a
  else -> b
}
