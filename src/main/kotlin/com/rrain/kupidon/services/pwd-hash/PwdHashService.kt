package com.rrain.kupidon.services.`pwd-hash`

import com.rrain.kupidon.services.env.Env
import io.ktor.server.application.*
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64



fun main() {
  
  /*"passwordToHash".let { println("hash for $it: ${generateUserPwdHash(
    it,
    // PRIVATE CONFIG
    PwdHashing.Config(
      algorithm = "",
      secret = "",
      iterations = 0,
      hashLen = 0,
    )
  )}") }*/
  
  //println("random pwd salt (PWD_HASHING_SECRET): ${generateRandomPwdSalt()}")
  
}





fun generateUserPwdHash(pwdToHash: String, config: PwdHashService.Config): String {
  PwdHashService.config = config
  val hash = PwdHashService.generateHash(pwdToHash)
  return hash
}


fun generateRandomPwdSalt(): String = ByteArray(16)
  .also { SecureRandom().nextBytes(it) }
  .let { Base64.Default.encode(it) }




fun Application.configurePwdHashService() {
  
  PwdHashService.run {
    config = PwdHashService.Config(
      algorithm = Env.dbUserPwdHashingAlgorithm,
      secret = Env.dbUserPwdHashingSecret,
      iterations = Env.dbUserPwdHashingIterations,
      hashLen = Env.dbUserPwdHashingHashLen,
    )
  }
}



object PwdHashService {
  
  data class Config(
    val algorithm: String,
    val secret: String,
    val iterations: Int,
    val hashLen: Int,
  )
  
  lateinit var config: Config
  
  fun generateHash(pwd: String): String {
    val combinedSalt: ByteArray = "${config.secret}".toByteArray(Charsets.UTF_8)
    val factory: SecretKeyFactory = SecretKeyFactory.getInstance(config.algorithm)
    val spec: KeySpec = PBEKeySpec(
      pwd.toCharArray(),
      combinedSalt,
      config.iterations,
      config.hashLen
    )
    val key: SecretKey = factory.generateSecret(spec)
    val hash: String = key.encoded.let { Base64.Default.encode(it) }
    return hash
  }
  
  fun checkPwd(pwd: String, hash: String): Boolean {
    val pwdHash: String = generateHash(pwd)
    return pwdHash == hash
  }
  
}

