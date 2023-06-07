package com.rrain.kupidon.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.routes.authBaseRoute
import com.rrain.kupidon.routes.refreshRoute
import com.rrain.kupidon.util.get
import com.rrain.kupidon.util.zonedNow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import io.ktor.util.date.*
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration


fun Application.configureJwtService(){
  
  val appConfig = environment.config
  
  JwtService.run {
    accessTokenSecret = appConfig["jwt.access-token.secret"]
    accessTokenLifetime = appConfig["jwt.access-token.lifetime"].run(Duration::parse)
    refreshTokenSecret = appConfig["jwt.refresh-token.secret"]
    refreshTokenLifetime = appConfig["jwt.refresh-token.lifetime"].run(Duration::parse)
  }
  
}


object JwtService {
  
  val refreshTokenCookieName = "refreshToken"
  val accessTokenRolesClaimName = "roles"
  
  lateinit var accessTokenSecret: String
  var accessTokenLifetime: Duration = Duration.parse("5m") // 5 minutes expiration
  
  lateinit var refreshTokenSecret: String
  var refreshTokenLifetime: Duration = Duration.parse("30d") // 30 days expiration
  

  
  fun generateAccessToken(id: String, roles: Set<Role>): String {
    /*val accessJwt = object {
      //val audience = appConfig["jwt.access-token.audience"]
      //val issuer = appConfig["jwt.access-token.issuer"]
      val secret = appConfig["jwt.access-token.secret"]
    }*/
    
    val secret = accessTokenSecret
    val lifetime = accessTokenLifetime
    
    val accessToken =  JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id) // determines user
      //.withAudience(accessJwt.audience) // todo what is it???
      //.withIssuer(accessJwt.issuer) // todo what is it???
      //.withClaim("login", login)
      .withArrayClaim(accessTokenRolesClaimName, roles.map { it.toString() }.toTypedArray())
      .sign(Algorithm.HMAC256(secret))
    return accessToken
  }

  
  fun generateRefreshToken(id: String): String {
    /*val refreshJwt = object {
      //val audience = appConfig["jwt.refresh-token.audience"]
      //val issuer = appConfig["jwt.refresh-token.issuer"]
      val secret = appConfig["jwt.refresh-token.secret"]
    }*/
    
    val secret = refreshTokenSecret
    val lifetime = refreshTokenLifetime
    
    val refreshToken = JWT.create()
      .withExpiresAt(zonedNow().plus(lifetime.toJavaDuration()).toInstant())
      .withSubject(id) // determines user
      //.withAudience(refreshJwt.audience) // todo what is it???
      //.withIssuer(refreshJwt.issuer) // todo what is it???
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
      path = authBaseRoute, // only one path can be set
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
      path = authBaseRoute, // only one path can be set
      secure = true,
      httpOnly = true,
    )
  }

  
  fun getAccessTokenVerifier(
    secret: String,
  ): JWTVerifier {
    return JWT
      .require(Algorithm.HMAC256(secret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }

  
  fun getRefreshTokenVerifier(
    secret: String,
  ): JWTVerifier {
    return JWT
      .require(Algorithm.HMAC256(secret))
      //.withAudience(jwtAudience)
      //.withIssuer(jwtIssuer)
      .build()
  }

}

