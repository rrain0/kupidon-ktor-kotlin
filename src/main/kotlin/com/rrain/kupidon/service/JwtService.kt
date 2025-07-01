package com.rrain.kupidon.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.rrain.kupidon.model.Role
import com.rrain.kupidon.route.`response-errors`.CodeMsg
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.`util-ktor`.application.get
import com.rrain.`util-ktor`.application.appConfig
import com.rrain.util.`date-time`.now
import com.rrain.util.uuid.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.security.SecureRandom
import java.util.UUID.randomUUID
import kotlin.io.encoding.Base64
import kotlin.time.Duration



fun main() {
  println("random JWT SECRET: ${generateJwtSecret()}")
  println("generated access token: ${generateCustomAccessToken()}")
}


fun generateJwtSecret(): String = (
  ByteArray(64)
    .also { SecureRandom().nextBytes(it) }
    .let { Base64.Default.encode(it) }
)


fun generateCustomAccessToken(): TokenData {
  JwtService.config = JwtService.Config(
    accessTokenSecret =
      "SECRET",
    accessTokenLifetime = Duration.parse("${365 * 2}d"),
    
    refreshTokenSecret =
      "SECRET",
    refreshTokenLifetime = Duration.parse("30d"),
    
    emailVerifyAccessTokenLifetime = Duration.parse("1d"),
  )
  return JwtService.newAccessToken(
    "USER_ID", setOf(), randomUUID().toString(), now(),
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
  
  
  
  
  val DecodedJWT.userIdStr get() = this.subject!!
  val DecodedJWT.userId get() = this.userIdStr.toUuid()
  val DecodedJWT.sessionIdStr get() = this.claims["sessionId"]!!.asString()
  val DecodedJWT.sessionId get() = this.sessionIdStr.toUuid()
  
  
  
  
  fun newAccessToken(
    id: String,
    roles: Set<Role>,
    sessionId: String,
    now: Instant,
  ): TokenData {
    val secret = config.accessTokenSecret
    val lifetime = config.accessTokenLifetime
    val expiresAt = now.plus(lifetime)
    
    val accessToken = JWT.create()
      // Determines when token will expire
      .withExpiresAt(expiresAt.toJavaInstant())
      // Identifies the principal (user, entity) the JWT represents, e.g., "user123"
      .withSubject(id)
      // Specifies the intended recipient(s) of the JWT, e.g., "api.example.com"
      //.withAudience(accessJwt.audience)
      // Who issued the token (source)
      //.withIssuer(accessJwt.issuer)
      // Some additional optional parameter
      //.withClaim("realm", realm)
      .withClaim(config.accessTokenRolesClaimName, roles.map { it.toString() })
      .withClaim("sessionId", sessionId)
      .sign(config.buildAlgorithm(secret))
    
    return TokenData(accessToken, expiresAt)
  }
  
  
  fun newRefreshToken(
    id: String,
    sessionId: String,
    now: Instant,
  ): TokenData {
    val secret = config.refreshTokenSecret
    val lifetime = config.refreshTokenLifetime
    val expiresAt = now.plus(lifetime)
    
    val refreshToken = JWT.create()
      .withExpiresAt(expiresAt.toJavaInstant())
      .withSubject(id)
      .withClaim("sessionId", sessionId)
      .sign(config.buildAlgorithm(secret))
    return TokenData(refreshToken, expiresAt)
  }
  
  
  fun newVerificationAccessToken(
    id: String,
    email: String,
    now: Instant,
  ): TokenData {
    val secret = config.accessTokenSecret
    val lifetime = config.emailVerifyAccessTokenLifetime
    val expiresAt = now.plus(lifetime)
    
    val accessToken = JWT.create()
      .withExpiresAt(expiresAt.toJavaInstant())
      .withSubject(id)
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaim("email", email)
      .sign(config.buildAlgorithm(secret))
    return TokenData(accessToken, expiresAt)
  }
  
  
  
  
  fun getRefreshTokenCookie(
    refreshToken: String,
    // according to RFC you can specify only domain (without port) for all its ports
    domain: String
  ) = Cookie(
    name = config.refreshTokenCookieName,
    value = refreshToken,
    expires = JWT.decode(refreshToken).expiresAtAsInstant.toGMTDate(),
    domain = domain,
    path = ApiV1Routes.auth, // only one path can be set
    secure = true,
    httpOnly = true,
  )
  
  
  fun getExpiredRefreshTokenCookie(
    // according to RFC you can specify only domain (without port) for all its ports
    domain: String
  ) = Cookie(
    name = config.refreshTokenCookieName,
    value = "",
    maxAge = 0,
    domain = domain,
    path = ApiV1Routes.auth, // only one path can be set
    secure = true,
    httpOnly = true,
  )

  
  val accessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.accessTokenSecret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .withClaimPresence("sessionId")
      .build()
  }
  
  val refreshTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.refreshTokenSecret))
      .withClaimPresence("sessionId")
      .build()
  }
  
  val emailValidationAccessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.accessTokenSecret))
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaimPresence("email")
      .build()
  }

}




data class TokenData(
  val token: String,
  val expiresAt: Instant,
)




// Token was encoded by wrong algorithm. Required HMAC256.
val ErrTokenAlgorithmMismatch get() = CodeMsg(
  "TOKEN_ALGORITHM_MISMATCH", 
  "Token was encoded by wrong algorithm, required ${JwtService.config.algorithmName}"
)
// Damaged Token - Токен повреждён и не может быть декодирован
val ErrTokenDamaged = CodeMsg(
  "TOKEN_DAMAGED",
  "Token is damaged - failed to decode JSON token data"
)
// Modified Token - Токен умышленно модифицирован (подделан)
val ErrTokenModified = CodeMsg(
  "TOKEN_MODIFIED",
  "Token was modified"
)
// Token has expired
val ErrTokenExpired = CodeMsg(
  "TOKEN_EXPIRED",
  "Token has expired"
)
val ErrTokenLacksOfClaim = CodeMsg(
  "TOKEN_LACKS_OF_CLAIM",
  "Token lacks some required claims"
)
val ErrTokenClaimValueIsIncorrect = CodeMsg(
  "TOKEN_CLAIM_VALUE_IS_INCORRECT",
  "Token has incorrect claim value"
)
// Common Token Verification Exception
val ErrTokenUnknownVerificationError = CodeMsg(
  "UNKNOWN_VERIFICATION_ERROR",
  "Unknown Token Verification Exception"
)

