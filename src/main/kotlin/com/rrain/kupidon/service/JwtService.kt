package com.rrain.kupidon.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.rrain.kupidon.model.Role
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.`util-ktor`.application.get
import com.rrain.util.`date-time`.zonedNow
import com.rrain.`util-ktor`.application.appConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration
import kotlin.time.toJavaDuration



fun main() {
  println("random JWT SECRET: ${generateJwtSecret()}")
  println("generated access token: ${generateCustomAccessToken()}")
}


@OptIn(ExperimentalEncodingApi::class)
fun generateJwtSecret(): String = ByteArray(64)
  .also { SecureRandom().nextBytes(it) }
  .let { Base64.Default.encode(it) }


fun generateCustomAccessToken(): String {
  JwtService.config = JwtService.Config(
    accessTokenSecret =
      "SECRET",
    accessTokenLifetime = Duration.parse("${365 * 2}d"),
    
    refreshTokenSecret =
      "SECRET",
    refreshTokenLifetime = Duration.parse("30d"),
    
    emailVerifyAccessTokenLifetime = Duration.parse("1d"),
  )
  return JwtService.generateAccessToken(
    "USER_ID", setOf()
  )
}




fun Application.configureJwtService() {
  
  JwtService.config = JwtService.Config(
    accessTokenSecret = appConfig["jwt.access-token.secret"],
    accessTokenLifetime = appConfig["jwt.access-token.lifetime"].run(Duration::parse),
    
    refreshTokenSecret = appConfig["jwt.refresh-token.secret"],
    refreshTokenLifetime = appConfig["jwt.refresh-token.lifetime"].run(Duration::parse),
    
    emailVerifyAccessTokenLifetime =
      appConfig["jwt.email-verify-access-token.lifetime"].run(Duration::parse),
  )
  
}




enum class AccessTokenType {
  EMAIL_VERIFICATION,
}



object JwtService {
  
  data class Config(
    val buildAlgorithm: (secret: String) -> Algorithm = { secret -> Algorithm.HMAC256(secret) },
    
    val accessTokenRolesClaimName: String = "roles",
    val accessTokenSecret: String,
    val accessTokenLifetime: Duration = Duration.parse("3m"), // 3 minutes expiration
    
    val refreshTokenCookieName: String = "refreshToken",
    val refreshTokenSecret: String,
    val refreshTokenLifetime: Duration = Duration.parse("30d"), // 30 days expiration
    
    val emailVerifyAccessTokenLifetime: Duration = Duration.parse("1d") // 1 day expiration
  ) {
    val algorithmName = buildAlgorithm("").name
  }
  
  lateinit var config: Config
  
  fun DecodedJWT.getUserId() = this.subject!!
  
  fun generateAccessToken(id: String, roles: Set<Role>): String {
    val secret = config.accessTokenSecret
    val lifetime = config.accessTokenLifetime
    
    val accessToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      // Determines user
      .withSubject(id)
      // Describes the recipient of the token (instead of subject)
      //.withAudience(accessJwt.audience)
      // The entity that issues the token
      //.withIssuer(accessJwt.issuer)
      //.withClaim("login", login)
      // An optional parameter providing additional context or scope
      //.withClaim("realm", realm)
      .withClaim(config.accessTokenRolesClaimName, roles.map { it.toString() })
      .sign(config.buildAlgorithm(secret))
    return accessToken
  }
  
  
  fun generateVerificationAccessToken(id: String, email: String): String {
    val secret = config.accessTokenSecret
    val lifetime = config.emailVerifyAccessTokenLifetime
    
    val accessToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id)
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaim("email", email)
      .sign(config.buildAlgorithm(secret))
    return accessToken
  }

  
  fun generateRefreshToken(id: String): String {
    val secret = config.refreshTokenSecret
    val lifetime = config.refreshTokenLifetime
    
    val refreshToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id) // determines user
      //.withAudience(refreshJwt.audience)
      //.withIssuer(refreshJwt.issuer)
      .sign(config.buildAlgorithm(secret))
    return refreshToken
  }
  
  
  fun generateRefreshTokenCookie(
    refreshToken: String,
    // according to RFC you can specify only domain (without port) for all its ports
    domain: String
  ): Cookie {
    return Cookie(
      name = config.refreshTokenCookieName,
      value = refreshToken,
      expires = JWT.decode(refreshToken).expiresAtAsInstant.toGMTDate(),
      domain = domain,
      path = ApiV1Routes.auth, // only one path can be set
      secure = true,
      httpOnly = true,
    )
  }
  
  
  fun generateExpiredRefreshTokenCookie(
    // according to RFC you can specify only domain (without port) for all its ports
    domain: String
  ): Cookie {
    return Cookie(
      name = config.refreshTokenCookieName,
      value = "",
      maxAge = 0,
      domain = domain,
      path = ApiV1Routes.auth, // only one path can be set
      secure = true,
      httpOnly = true,
    )
  }

  
  val accessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.accessTokenSecret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }
  
  val emailValidationAccessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.accessTokenSecret))
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaimPresence("email")
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }
  
  val refreshTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.refreshTokenSecret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }

}




// Token was encoded by wrong algorithm. Required HMAC256.
object ErrTokenAlgorithmMismatch {
  val code = "TOKEN_ALGORITHM_MISMATCH"
  val msg = "Token was encoded by wrong algorithm, required ${JwtService.config.algorithmName}"
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

