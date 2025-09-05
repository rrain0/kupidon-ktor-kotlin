package com.rrain.utils.base.collections

import java.util.concurrent.ConcurrentHashMap



fun <K, V> concurrentMapOf() = ConcurrentHashMap<K, V>()
fun <K, V> concurrentMapOf(vararg pairs: Pair<K, V>) = ConcurrentHashMap<K, V>().apply { putAll(pairs) }

fun <T> concurrentSetOf() = ConcurrentHashMap.newKeySet<T>()
fun <T> concurrentSetOf(vararg elements: T) = ConcurrentHashMap.newKeySet<T>().apply { addAll(elements) }