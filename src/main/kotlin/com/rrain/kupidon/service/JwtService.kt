package com.rrain.kupidon.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.rrain.kupidon.model.Role
import com.rrain.kupidon.route.route.auth.AuthRoutes
import com.rrain.kupidon.util.get
import com.rrain.kupidon.util.zonedNow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import io.ktor.util.date.*
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration
import kotlin.time.toJavaDuration



fun main() {
  println("random JWT SECRET: ${generateJwtSecret()}")
}


@OptIn(ExperimentalEncodingApi::class)
fun generateJwtSecret(): String = ByteArray(64)
  .also { SecureRandom().nextBytes(it) }
  .let { Base64.Default.encode(it) }




fun Application.configureJwtService(){
  
  val appConfig = environment.config
  
  JwtService.run {
    accessTokenSecret = appConfig["jwt.access-token.secret"]
    accessTokenLifetime = appConfig["jwt.access-token.lifetime"].run(Duration::parse)
    emailVerifyAccessTokenLifetime =
      appConfig["jwt.email-verify-access-token.lifetime"].run(Duration::parse)
    refreshTokenSecret = appConfig["jwt.refresh-token.secret"]
    refreshTokenLifetime = appConfig["jwt.refresh-token.lifetime"].run(Duration::parse)
  }
  
}




// Token was encoded by wrong algorithm. Required HMAC256.
object ErrTokenAlgorithmMismatch {
  val code = "TOKEN_ALGORITHM_MISMATCH"
  val msg = "Token was encoded by wrong algorithm, required ${JwtService.algorithmName}"
}
// Damaged Token - Токен повреждён и не может быть декодирован
object ErrTokenDamaged {
  val code = "TOKEN_DAMAGED"
  val msg = "Token is damaged - failed to decode JSON token data"
}
// Modified Token - Токен умышленно модифицирован (подделан)
object ErrTokenModified {
  val code = "TOKEN_MODIFIED"
  val msg = "Token was modified"
}
// Token has expired
object ErrTokenExpired {
  val code = "TOKEN_EXPIRED"
  val msg = "Token has expired"
}
object ErrTokenLacksOfClaim {
  val code = "TOKEN_LACKS_OF_CLAIM"
  val msg = "Token lacks some required claims"
}
object ErrTokenClaimValueIsIncorrect {
  val code = "TOKEN_CLAIM_VALUE_IS_INCORRECT"
  val msg = "Token has incorrect claim value"
}
// Common Token Verification Exception
object ErrTokenUnknownVerificationError {
  val code = "UNKNOWN_VERIFICATION_ERROR"
  val msg = "Unknown Token Verification Exception"
}





enum class AccessTokenType {
  EMAIL_VERIFICATION,
}



object JwtService {
  
  const val refreshTokenCookieName = "refreshToken"
  const val accessTokenRolesClaimName = "roles"
  
  lateinit var accessTokenSecret: String
  var accessTokenLifetime: Duration = Duration.parse("3m") // 3 minutes expiration
  var emailVerifyAccessTokenLifetime: Duration = Duration.parse("1d") // 1 day expiration
  
  lateinit var refreshTokenSecret: String
  var refreshTokenLifetime: Duration = Duration.parse("30d") // 30 days expiration
  
  const val algorithmName = "HS256"
  
  fun generateAccessToken(id: String, roles: Set<Role>): String {
    val secret = accessTokenSecret
    val lifetime = accessTokenLifetime
    
    val accessToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id) // determines user
      //.withAudience(accessJwt.audience)
      //.withIssuer(accessJwt.issuer)
      //.withClaim("login", login)
      .withClaim(accessTokenRolesClaimName, roles.map { it.toString() })
      .sign(Algorithm.HMAC256(secret))
    return accessToken
  }
  
  
  fun generateVerificationAccessToken(id: String, email: String): String {
    val secret = accessTokenSecret
    val lifetime = emailVerifyAccessTokenLifetime
    
    val accessToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id)
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaim("email", email)
      .sign(Algorithm.HMAC256(secret))
    return accessToken
  }

  
  fun generateRefreshToken(id: String): String {
    val secret = refreshTokenSecret
    val lifetime = refreshTokenLifetime
    
    val refreshToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id) // determines user
      //.withAudience(refreshJwt.audience)
      //.withIssuer(refreshJwt.issuer)
      .sign(Algorithm.HMAC256(secret))
    return refreshToken
  }
  
  
  fun generateRefreshTokenCookie(
    refreshToken: String,
    domain: String // according to RFC you can specify only domain (without port) for all its ports
  ): Cookie {
    return Cookie(
      name = refreshTokenCookieName,
      value = refreshToken,
      expires = JWT.decode(refreshToken).expiresAtAsInstant.toGMTDate(),
      domain = domain,
      path = AuthRoutes.base, // only one path can be set
      secure = true,
      httpOnly = true,
    )
  }
  
  
  fun generateRefreshTokenExpiredCookie(
    domain: String // according to RFC you can specify only domain (without port) for all its ports
  ): Cookie {
    return Cookie(
      name = refreshTokenCookieName,
      value = "",
      expires = GMTDate.START,
      domain = domain,
      path = AuthRoutes.base, // only one path can be set
      secure = true,
      httpOnly = true,
    )
  }

  
  val accessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(Algorithm.HMAC256(accessTokenSecret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }
  
  val emailValidationAccessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(Algorithm.HMAC256(accessTokenSecret))
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaimPresence("email")
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }
  
  val refreshTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(Algorithm.HMAC256(refreshTokenSecret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }

}

