package com.rrain.utils.base.`delegated-prop`

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


// get & set with no backing field
// Use it for local vals, where native kotlin getters are forbidden.
fun <T> getSet(
  getter: () -> T,
  setter: (value: T) -> Unit,
) = object : ReadWriteProperty<Any?, T> {
  override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter()
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setter(value)
}


// get & set with embedded backing field
// Use it for local vals, where native kotlin getters are forbidden.
fun <T> getSetV(
  initialValue: T,
  getter: (fieldValue: T) -> T = { it },
  setter: (value: T) -> T = { it },
) = object : ReadWriteProperty<Any?, T> {
  var fieldValue: T = initialValue
  override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter(fieldValue)
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    fieldValue = setter(value)
  }
}




fun main() {
  // Using with external backing field
  var intValue = 0
  var int1 by getSet({ intValue + 1 }, { intValue = it - 1 })
  // ERROR - Use get / set is not possible for local variables
  // var int1
  //   get() = intValue + 1
  //   set(v) { intValue = v - 1 }
  
  
  // Using with embeddedBackingFiled
  var int2 by getSetV(0, { it + 1 }, { it - 1 })
  // ERROR - Use get / set is not possible for local variables
  // var int1 = 0
  //   get() = field + 1
  //   set(v) { field = v - 1 }
}