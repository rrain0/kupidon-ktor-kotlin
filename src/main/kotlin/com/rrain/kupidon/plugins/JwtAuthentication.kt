package com.rrain.kupidon.plugins

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.rrain.kupidon.routes.`response-errors`.CodeMsg
import com.rrain.kupidon.routes.`response-errors`.respond401Unauthorized
import com.rrain.kupidon.services.jwt.AccessToken
import com.rrain.kupidon.services.jwt.ErrTokenAlgorithmMismatch
import com.rrain.kupidon.services.jwt.ErrTokenDamaged
import com.rrain.kupidon.services.jwt.ErrTokenExpired
import com.rrain.kupidon.services.jwt.ErrTokenLacksOfClaim
import com.rrain.kupidon.services.jwt.ErrTokenModified
import com.rrain.kupidon.services.jwt.ErrTokenUnknownVerificationError
import io.ktor.server.auth.*
import io.ktor.server.application.*
import io.ktor.server.routing.RoutingContext
import java.util.UUID




fun Application.configureJwtAuthentication() {
  authentication {
    register(MyJWTAuthenticationProvider())
  }
}




val RoutingContext.authUserId: UUID get() = call.principal<AccessToken>()!!.userId



class MyJWTAuthenticationProvider : AuthenticationProvider(Config()) {
  
  class Config : AuthenticationProvider.Config(null)
  
  override suspend fun onAuthenticate(context: AuthenticationContext) {
    val call = context.call
    val authHeader = call.request.headers["Authorization"]
    if (authHeader == null) {
      return call.respond401Unauthorized(ErrNoAuthHeader)
    }
    if (!authHeader.startsWith("Bearer ")) {
      return call.respond401Unauthorized(ErrAuthHeaderWrongFormat)
    }
    
    
    val accessToken = authHeader.substring("Bearer ".length)
    if (accessToken.isEmpty()) {
      return call.respond401Unauthorized(ErrEmptyToken)
    }
    
    val decodedAccess =
      try {
        AccessToken(accessToken)
      }
      // Token was encoded by wrong algorithm. Required <algorithm-name>.
      catch (ex: AlgorithmMismatchException) {
        return call.respond401Unauthorized(ErrTokenAlgorithmMismatch)
      }
      // Damaged Token - Токен повреждён и не может быть декодирован
      catch (ex: JWTDecodeException) {
        return call.respond401Unauthorized(ErrTokenDamaged)
      }
      // Modified Token - Токен умышленно модифицирован (подделан)
      catch (ex: SignatureVerificationException) {
        return call.respond401Unauthorized(ErrTokenModified)
      }
      // Token has expired
      catch (ex: TokenExpiredException) {
        return call.respond401Unauthorized(ErrTokenExpired)
      }
      // Required claim is missing
      catch (ex: MissingClaimException) {
        return call.respond401Unauthorized(ErrTokenLacksOfClaim)
      }
      // Common Verification Exception
      catch (ex: Exception) {
        println("AAAAAAAAA")
        ex.printStackTrace()
        return call.respond401Unauthorized(ErrTokenUnknownVerificationError)
      }
    
    context.principal(decodedAccess)
  }
  
}



val ErrNoAuthHeader = CodeMsg(
  "NO_AUTHORIZATION_HEADER",
  """No "Authorization" header is present"""
)
val ErrAuthHeaderWrongFormat = CodeMsg(
  "AUTHORIZATION_HEADER_WRONG_FORMAT",
  """"Authorization" header must start with "Bearer """"
)
val ErrEmptyToken = CodeMsg(
  "EMPTY_TOKEN",
  """"Authorization" header must contain non-empty access-token "Bearer <access-token>""""
)




