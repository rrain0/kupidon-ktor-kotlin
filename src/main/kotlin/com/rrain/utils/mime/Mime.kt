package com.rrain.utils.mime

import com.rrain.utils.base.print.println



fun String.mimeToExt(): String? = when (this) {
  "image/jpeg" -> "jpg"
  "image/png" -> "png"
  "image/webp" -> "webp"
  "image/heic" -> "heic"
  "image/avif" -> "avif"
  else -> null
}
fun String.mimeToExtOrEmpty() = mimeToExt() ?: ""



fun String.extToMime(): String? = when (this) {
  "jpg" -> "image/jpeg"
  "jpeg" -> "image/jpeg"
  "jpe" -> "image/jpeg"
  "jif" -> "image/jpeg"
  "jfif" -> "image/jpeg"
  "jfi" -> "image/jpeg"
  
  "png" -> "image/png"
  
  "webp" -> "image/webp"
  
  "heic" -> "image/heic"
  
  "avif" -> "image/avif"
  
  else -> null
}
fun String.extToMimeOrEmpty() = extToMime() ?: ""




fun main() {
  println("image/jpeg".mimeToExt()) // jpeg
  println("jpg".extToMime()) // image/jpeg
  println()
  println("image/webp".mimeToExt()) //
  println("webp".extToMime()) // image/
  println()
  println("image/heic".mimeToExt()) //
  println("heic".extToMime()) // image/
  println()
  println("image/avif".mimeToExt()) //
  println("avif".extToMime()) // image/
  println()
  println("".mimeToExt()) // null
  println("".extToMime()) // null
  println()
  println("image/aaa".mimeToExt()) // null
  println("aaa".extToMime()) // null
}

