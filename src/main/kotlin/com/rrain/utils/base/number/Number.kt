package com.rrain.utils.base.number



inline infix fun Int.ifZero(block: () -> Int): Int {
  return if (this == 0) block() else this
}
inline infix fun Double.ifZero(block: () -> Double): Double {
  return if (this == 0.0) block() else this
}