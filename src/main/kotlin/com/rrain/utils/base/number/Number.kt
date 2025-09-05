package com.rrain.utils.base.number



inline infix fun Int.ifZero(block: () -> Int): Int = (
  if (this == 0) block() else this
                                                     )
inline infix fun Double.ifZero(block: () -> Double): Double = (
  if (this == 0.0) block() else this
                                                              )