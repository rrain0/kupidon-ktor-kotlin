package com.rrain.utils.base.`delegated-prop`

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


// Use it for local vals, where native kotlin getters are forbidden.
// val i by getIt { 1 }
// Name 'getIt' instead of 'get' to avoid naming conflicts with other contexts.
fun <T> getIt(getter: () -> T) = object : ReadOnlyProperty<Any?, T> {
  override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter()
}


fun main() {
  //val int1 get() = 1 // ERROR - Use get / set is not possible for local variables
  val int2 by getIt { 1 }
}