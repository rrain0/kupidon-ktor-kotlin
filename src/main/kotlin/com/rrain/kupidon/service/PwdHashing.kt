package com.rrain.kupidon.service

import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi



fun main() {
  
  /*"passwordToHash".let { println("hash for $it: ${generateUserPwdHash(
    it,
    // PRIVATE CONFIG
    PwdHashingInfo(
      algorithm = "",
      secret = "",
      iterations = 0,
      hashLen = 0,
    )
  )}") }*/
  
  //println("random pwd salt (PWD_HASHING_SECRET): ${generateRandomPwdSalt()}")
  
}





fun generateUserPwdHash(pwdToHash: String, pwdHashingInfo: PwdHashingInfo): String {
  PwdHashing.pwdHashingInfo = pwdHashingInfo
  val hash = PwdHashing.generateHash(pwdToHash)
  return hash
}


@OptIn(ExperimentalEncodingApi::class)
fun generateRandomPwdSalt(): String = ByteArray(16)
  .also { SecureRandom().nextBytes(it) }
  .let { Base64.Default.encode(it) }




fun Application.configurePwdHashing(){
  val appConfig = environment.config
  
  PwdHashing.run {
    pwdHashingInfo = PwdHashingInfo(
      algorithm = appConfig["db.user-pwd-hashing.algorithm"],
      secret = appConfig["db.user-pwd-hashing.secret"],
      iterations = appConfig["db.user-pwd-hashing.iterations"].toInt(),
      hashLen = appConfig["db.user-pwd-hashing.hash-len"].toInt(),
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