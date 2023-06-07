package com.rrain.kupidon.service

import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration


fun main() {
  val pwdHashingInfo = PwdHashingInfo(
    algorithm = "PBKDF2WithHmacSHA512",
    secret = "xGNw2g+wXCHg+YGyIB+72A==",
    iterations = 120000,
    hashLen = 256,
  )
  PwdHashing.pwdHashingInfo = pwdHashingInfo
  repeat(20){
    println(PwdHashing.generateHash("somepassword"))
  }
}


fun Application.configurePwdHashing(){
  val appConfig = environment.config
  
  PwdHashing.run {
    pwdHashingInfo = PwdHashingInfo(
      algorithm = appConfig["database.user-pwd-hashing.algorithm"],
      secret = appConfig["database.user-pwd-hashing.secret"],
      iterations = appConfig["database.user-pwd-hashing.iterations"].toInt(),
      hashLen = appConfig["database.user-pwd-hashing.hash-len"].toInt(),
    )
  }
}


object PwdHashing {
  
  lateinit var pwdHashingInfo: PwdHashingInfo
  
  @OptIn(ExperimentalEncodingApi::class)
  fun generateHash(pwd: String): String {
    val info = pwdHashingInfo
    val combinedSalt: ByteArray = "${info.secret}".toByteArray(Charsets.UTF_8)
    val factory: SecretKeyFactory = SecretKeyFactory.getInstance(info.algorithm)
    val spec: KeySpec = PBEKeySpec(
      pwd.toCharArray(),
      combinedSalt,
      info.iterations,
      info.hashLen
    )
    val key: SecretKey = factory.generateSecret(spec)
    val hash: String = key.encoded.let { Base64.Default.encode(it) }
    return hash
  }
  
  fun checkPwd(pwd: String, hash: String): Boolean {
    val pwdhash: String = generateHash(pwd)
    return pwdhash == hash
  }

}


data class PwdHashingInfo(
  val algorithm: String,
  val secret: String,
  val iterations: Int,
  val hashLen: Int,
)