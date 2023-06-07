package com.rrain.kupidon.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.util.get
import io.ktor.server.application.*


class AuthenticationException() : RuntimeException("Authentication with access token fails")

val authenticationPluginName = "access-token"

fun Application.configureJwtAuthentication() {
  
  val appConfig = environment.config
  
  authentication {
    jwt(authenticationPluginName) {
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
        
        // call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
      }
    }
  }
  
}
