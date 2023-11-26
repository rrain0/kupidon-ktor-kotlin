package com.rrain.kupidon.plugin

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.rrain.kupidon.service.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking





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
    
    provider {
      
      authenticate { auth -> runBlocking(Dispatchers.IO){
        val authHeader = auth.call.request.headers["Authorization"]
        if (authHeader==null)
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrNoAuthHeader)
        if (!authHeader.startsWith("Bearer "))
          return@runBlocking auth.call
          .respond(HttpStatusCode.Unauthorized, ErrAuthHeaderWrongFormat)
          
        
        val accessToken = authHeader.substring("Bearer ".length)
        if (accessToken.isEmpty())
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrEmptyToken)
        
        val verifier = JwtService.accessTokenVerifier
        val decodedJwt = try { verifier.verify(accessToken) }
        // Token was encoded by wrong algorithm. Required HMAC256.
        catch (ex: AlgorithmMismatchException){
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrTokenAlgorithmMismatch)
        }
        // Damaged Token - Токен повреждён и не может быть декодирован
        catch (ex: JWTDecodeException){
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrTokenDamaged)
        }
        // Modified Token - Токен умышленно модифицирован (подделан)
        catch (ex: SignatureVerificationException){
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrTokenModified)
        }
        // Token has expired
        catch (ex: TokenExpiredException){
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrTokenExpired)
        }
        // Common Verification Exception
        catch (ex: JWTVerificationException) {
          ex.printStackTrace()
          return@runBlocking auth.call
            .respond(HttpStatusCode.Unauthorized, ErrTokenUnknownVerificationError)
        }
        
        auth.principal(JWTPrincipal(decodedJwt))
      }}
      
      
    }
    
    
    
    
    
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
    
    
    
    
    /*provider { authenticate { auth ->
        val authHeader = auth.call.request.headers["Authorization"]
        authHeader ?: throw AuthenticationException(
          AuthErrorCode.NO_AUTHORIZATION_HEADER.name,
          AuthErrorCode.NO_AUTHORIZATION_HEADER.msg
        )
        if (!authHeader.startsWith("Bearer ")) throw AuthenticationException(
          AuthErrorCode.AUTHORIZATION_HEADER_WRONG_FORMAT.name,
          AuthErrorCode.AUTHORIZATION_HEADER_WRONG_FORMAT.msg
        )
        val accessToken = authHeader.substring("Bearer ".length)
        if (accessToken.isEmpty()) throw AuthenticationException(
          AuthErrorCode.EMPTY_TOKEN.name,
          AuthErrorCode.EMPTY_TOKEN.msg
        )
        
        val verifier = JwtService.accessTokenVerifier
        val decodedJwt = try { verifier.verify(accessToken) }
        catch (ex: AlgorithmMismatchException){ // Wrong algorithm is used to decode
          throw AuthenticationException(
            TokenError.TOKEN_WITH_WRONG_ALGORITHM.name,
            TokenError.TOKEN_WITH_WRONG_ALGORITHM.msg,
            ex
          )
        }
        catch (ex: JWTDecodeException){ // Failed to decode JSON data - Token is faulty
          throw AuthenticationException(
            TokenError.TOKEN_DAMAGED.name,
            TokenError.TOKEN_DAMAGED.msg,
            ex
          )
        }
        catch (ex: SignatureVerificationException){ // Modified Token
          throw AuthenticationException(
            TokenError.TOKEN_MODIFIED.name,
            TokenError.TOKEN_MODIFIED.msg,
            ex
          )
        }
        catch (ex: TokenExpiredException){ // Token has expired
          throw AuthenticationException(
            TokenError.TOKEN_EXPIRED.name,
            TokenError.TOKEN_EXPIRED.msg,
            ex
          )
        }
        catch (ex: JWTVerificationException) { // Common verification exception
          ex.printStackTrace()
          throw AuthenticationException(
            null,
            "Unknown Token Verification Exception",
            ex
          )
        }
        
        auth.principal(JWTPrincipal(decodedJwt))
      } }*/
    
    
  }
  
}
