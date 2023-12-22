package com.rrain.kupidon.util

import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi




fun main(){
  println("random pwd salt: ${generateRandomPwdSalt()}")
}

@OptIn(ExperimentalEncodingApi::class)
fun generateRandomPwdSalt(): String = ByteArray(16)
  .also { SecureRandom().nextBytes(it) }
  .let { Base64.Default.encode(it) }