package com.rrain.kupidon.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.routes.AuthRoutes
import com.rrain.kupidon.util.get
import com.rrain.kupidon.util.zonedNow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import io.ktor.util.date.*
import kotlin.time.Duration
import kotlin.time.toJavaDuration


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



enum class TokenError(val msg: String) {
  TOKEN_ALGORITHM_MISMATCH("Token was encoded by wrong algorithm, required ${JwtService.algorithmName}"),
  TOKEN_DAMAGED("Token is damaged - failed to decode JSON token data"),
  TOKEN_MODIFIED("Token was modified"),
  TOKEN_EXPIRED("Token has expired"),
  TOKEN_LACKS_OF_CLAIM("Token lacks some required claims"),
  TOKEN_CLAIM_VALUE_IS_INCORRECT("Token has incorrect claim value"),
  
  UNKNOWN_VERIFICATION_ERROR("Unknown Token Verification Exception"),
}



enum class AccessTokenType {
  EMAIL_VERIFICATION,
}

object JwtService {
  
  val refreshTokenCookieName = "refreshToken"
  val accessTokenRolesClaimName = "roles"
  
  lateinit var accessTokenSecret: String
  var accessTokenLifetime: Duration = Duration.parse("5m") // 5 minutes expiration
  var emailVerifyAccessTokenLifetime: Duration = Duration.parse("1d") // 1 day expiration
  
  lateinit var refreshTokenSecret: String
  var refreshTokenLifetime: Duration = Duration.parse("30d") // 30 days expiration
  
  val algorithmName = "HS256"
  
  fun generateAccessToken(id: String, roles: Set<Role>): String {
    val secret = accessTokenSecret
    val lifetime = accessTokenLifetime
    
    val accessToken =  JWT.create()
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

