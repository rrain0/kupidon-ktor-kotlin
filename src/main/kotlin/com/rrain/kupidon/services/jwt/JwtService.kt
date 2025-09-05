package com.rrain.kupidon.services.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.rrain.kupidon.models.Role
import com.rrain.kupidon.routes.`response-errors`.CodeMsg
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.env.Env
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.base.uuid.toUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.security.SecureRandom
import java.util.UUID
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


fun generateCustomAccessToken(): AccessToken {
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
    randomUUID(), setOf(), randomUUID(), now(),
  )
}




fun Application.configureJwtService() {
  
  JwtService.config = JwtService.Config(
    accessTokenSecret = Env.accessTokenSecret,
    accessTokenLifetime = Env.accessTokenLifetime,
    
    refreshTokenSecret = Env.refreshTokenSecret,
    refreshTokenLifetime = Env.refreshTokenLifetime,
    
    emailVerifyAccessTokenLifetime = Env.emailVerifyAccessTokenLifetime,
  )
  
}




enum class AccessTokenType {
  EMAIL_VERIFICATION,
}



object JwtService {
  
  data class Config(
    val buildAlgorithm: (secret: String) -> Algorithm = { secret -> Algorithm.HMAC256(secret) },
    
    val accessTokenSecret: String,
    val accessTokenLifetime: Duration = Duration.parse("3m"), // 3 minutes expiration
    
    val refreshTokenSecret: String,
    val refreshTokenLifetime: Duration = Duration.parse("30d"), // 30 days expiration
    
    val emailVerifyAccessTokenLifetime: Duration = Duration.parse("1d"), // 1 day expiration
  ) {
    val algorithmName = buildAlgorithm("").name
  }
  
  lateinit var config: Config
  
  
  
  
  fun newAccessToken(
    id: UUID,
    roles: Set<Role>,
    sessionId: UUID,
    now: Instant,
  ): AccessToken {
    val secret = config.accessTokenSecret
    val lifetime = config.accessTokenLifetime
    val expiresAt = now.plus(lifetime)
    val sessionLifetime = config.refreshTokenLifetime
    val sessionExpiresAt = now.plus(sessionLifetime)
    
    val accessToken = JWT.create()
      // Determines when token will expire
      .withExpiresAt(expiresAt.toJavaInstant())
      // Identifies the principal (user, entity) the JWT represents, e.g., "user123"
      .withSubject(id.toString())
      // Specifies the intended recipient(s) of the JWT, e.g., "api.example.com"
      //.withAudience(accessJwt.audience)
      // Who issued the token (source)
      //.withIssuer(accessJwt.issuer)
      // Some additional optional parameter
      //.withClaim("realm", realm)
      .withClaim("sessionId", sessionId.toString())
      .withClaim("sessionExpiresAt", sessionExpiresAt.toJavaInstant())
      .withClaim("roles", roles.map { it.toString() })
      //.withClaim("test", "1")
      .sign(config.buildAlgorithm(secret))
    
    return AccessToken(accessToken)
  }
  
  val accessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.accessTokenSecret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .withClaimPresence("sessionId")
      .withClaimPresence("sessionExpiresAt")
      //.withClaimPresence("test")
      .build()
  }
  
  
  
  fun newRefreshToken(
    id: UUID,
    sessionId: UUID,
    now: Instant,
  ): RefreshToken {
    val secret = config.refreshTokenSecret
    val lifetime = config.refreshTokenLifetime
    val expiresAt = now.plus(lifetime)
    val sessionLifetime = config.refreshTokenLifetime
    val sessionExpiresAt = now.plus(sessionLifetime)
    
    val refreshToken = JWT.create()
      .withExpiresAt(expiresAt.toJavaInstant())
      .withSubject(id.toString())
      .withClaim("sessionId", sessionId.toString())
      .withClaim("sessionExpiresAt", sessionExpiresAt.toJavaInstant())
      //.withClaim("test", "1")
      .sign(config.buildAlgorithm(secret))
    return RefreshToken(refreshToken)
  }
  
  val refreshTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.refreshTokenSecret))
      .withClaimPresence("sessionId")
      .withClaimPresence("sessionExpiresAt")
      //.withClaimPresence("test")
      .build()
  }
  
  
  
  fun newEmailVerificationAccessToken(
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
  
  val emailValidationAccessTokenVerifier: JWTVerifier by lazy {
    JWT
      .require(config.buildAlgorithm(config.accessTokenSecret))
      .withClaim("type", AccessTokenType.EMAIL_VERIFICATION.name)
      .withClaimPresence("email")
      .build()
  }
  
  
  
  
  fun getRefreshTokenCookie(
    refreshToken: String,
    // according to RFC you can specify only domain (without port) for all its ports
    domain: String
  ) = Cookie(
    name = "refreshToken",
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
    name = "refreshToken",
    value = "",
    maxAge = 0,
    domain = domain,
    path = ApiV1Routes.auth, // only one path can be set
    secure = true,
    httpOnly = true,
  )

}



data class AccessToken(
  val token: String,
  val noVerify: Boolean = false,
) {
  val decodedToken = if (noVerify) JWT.decode(token)
    else JwtService.accessTokenVerifier.verify(token)
  
  val expiresAt: Instant = decodedToken.expiresAtAsInstant.toKotlinInstant()
  
  val userIdStr: String = decodedToken.subject
  val userId: UUID = userIdStr.toUuid()
  
  val sessionIdStr: String = decodedToken.claims["sessionId"]!!.asString()
  val sessionId: UUID = sessionIdStr.toUuid()
  
  val sessionExpiresAt: Instant = decodedToken.claims["sessionExpiresAt"]!!
    .asInstant().toKotlinInstant()
  
  val roles: Set<Role> = decodedToken.claims["roles"]
    ?.takeIf { !it.isMissing && !it.isNull }
    ?.asList(String::class.java)
    ?.map { Role.valueOf(it) }
    ?.toSet()
    ?: emptySet()
}



data class RefreshToken(
  val token: String,
) {
  val decodedToken = JwtService.refreshTokenVerifier.verify(token)
  
  val expiresAt: Instant = decodedToken.expiresAtAsInstant.toKotlinInstant()
  
  val userIdStr: String = decodedToken.subject
  val userId: UUID = userIdStr.toUuid()
  
  val sessionIdStr: String = decodedToken.claims["sessionId"]!!.asString()
  val sessionId: UUID = sessionIdStr.toUuid()
  
  val sessionExpiresAt: Instant = decodedToken.claims["sessionExpiresAt"]!!
    .asInstant().toKotlinInstant()
}


data class TokenData(
  val token: String,
  val expiresAt: Instant,
)



val ApplicationCall.refreshTokenCookie get() = this.request.cookies["refreshToken"]




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

