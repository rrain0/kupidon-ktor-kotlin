package com.rrain.kupidon.plugin

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.rrain.kupidon.service.*
import com.rrain.util.uuid.toUuid
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.RoutingContext
import java.util.UUID




object ErrNoAuthHeader {
  val code = "NO_AUTHORIZATION_HEADER"
  val msg = """No "Authorization" header is present"""
}
object ErrAuthHeaderWrongFormat {
  val code = "AUTHORIZATION_HEADER_WRONG_FORMAT"
  val msg = """"Authorization" header must start with "Bearer """"
}
object ErrEmptyToken {
  val code = "EMPTY_TOKEN"
  val msg = """"Authorization" header must contain non-empty access-token "Bearer <access-token>""""
}



fun Application.configureJwtAuthentication() {
  
  authentication {
    register(MyJWTAuthenticationProvider())
    
    
    
    // Default ktor way
    // You can build more providers with distinct names with jwt("provider-name") { ... }
    /*
    jwt {
      // I don't need realms, audience, issuer
      
      //val jwtAudience = appConfig["jwt.access-token.audience"]
      //val jwtIssuer = appConfig["jwt.access-token.issuer"]
      val jwtSecret = appConfig["jwt.access-token.secret"]
      
      //realm = appConfig["jwt.access-token.realm"]
      
      verifier(
        JwtService.getAccessTokenVerifier(jwtSecret)
      )
      validate { credential ->
        //if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
        JWTPrincipal(credential.payload) // in the case of successful authentication return JWTPrincipal
      }
      challenge { defaultScheme, realm ->
        //this.call.authentication.allErrors.forEach { println("error: ${it.message}") }
        //this.call.authentication.allFailures.forEach { println("failure: ${it}") }
        throw AuthenticationException()
        // call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
      }
    }
    */
    
    
  }
  
}





class MyJWTAuthenticationProvider : AuthenticationProvider(Config()) {
  
  override suspend fun onAuthenticate(context: AuthenticationContext) {
    val authHeader = context.call.request.headers["Authorization"]
    if (authHeader == null) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrNoAuthHeader)
    }
    if (!authHeader.startsWith("Bearer ")) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrAuthHeaderWrongFormat)
    }
    
    
    val accessToken = authHeader.substring("Bearer ".length)
    if (accessToken.isEmpty()) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrEmptyToken)
    }
    
    val verifier = JwtService.accessTokenVerifier
    val decodedJwt = try { verifier.verify(accessToken) }
    // Token was encoded by wrong algorithm. Required <algorithm-name>.
    catch (ex: AlgorithmMismatchException) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrTokenAlgorithmMismatch)
    }
    // Damaged Token - Токен повреждён и не может быть декодирован
    catch (ex: JWTDecodeException) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrTokenDamaged)
    }
    // Modified Token - Токен умышленно модифицирован (подделан)
    catch (ex: SignatureVerificationException) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrTokenModified)
    }
    // Token has expired
    catch (ex: TokenExpiredException) {
      return context.call.respond(HttpStatusCode.Unauthorized, ErrTokenExpired)
    }
    // Common Verification Exception
    catch (ex: JWTVerificationException) {
      ex.printStackTrace()
      return context.call.respond(HttpStatusCode.Unauthorized, ErrTokenUnknownVerificationError)
    }
    
    context.principal(JWTPrincipal(decodedJwt))
  }
  
  
  class Config : AuthenticationProvider.Config(null)
}



val RoutingContext.authUserUuid: UUID get() = (
  this.call.principal<JWTPrincipal>()!!.subject!!.toUuid()
)




