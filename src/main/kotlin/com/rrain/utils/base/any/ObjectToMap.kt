package com.rrain.utils.base.any

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor




fun <T : Any> T.objectToMap(): Map<String, Any?> {
  return (this::class as KClass<T>).memberProperties.associate { prop ->
    prop.name to prop.get(this)?.let { value ->
      if (value::class.isData) value.objectToMap()
      else value
    }
  }
}


fun <T : Any> T.objectPrimaryPropsToMap(): Map<String, Any?> {
  val kClass = this::class as KClass<T>
  val primaryConstructorPropertyNames = kClass.primaryConstructor?.parameters?.map { it.name }
    ?: return mapOf()
  return kClass.memberProperties
    .mapNotNull { prop ->
      prop.name
        .takeIf { it in primaryConstructorPropertyNames }
        ?.let {
          it to prop.get(this)?.let { value ->
            if (value::class.isData) value.objectPrimaryPropsToMap()
            else value
          }
        }
    }
    .toMap()
}


