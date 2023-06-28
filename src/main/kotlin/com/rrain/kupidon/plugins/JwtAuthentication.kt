package com.rrain.kupidon.plugins

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.TokenError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


enum class AuthErrorCode(val msg: String) {
  NO_AUTHORIZATION_HEADER("""No "Authorization" header is present"""),
  AUTHORIZATION_HEADER_WRONG_FORMAT(""""Authorization" header must start with "Bearer """"),
  EMPTY_TOKEN(""""Authorization" header must contain non-empty access-token "Bearer <access-token>""""),
}

/*class AuthenticationException(
  val code: String? = null,
  msg: String,
  cause: Throwable? = null,
) : RuntimeException(msg, cause)*/


fun Application.configureJwtAuthentication() {
  
  authentication {
    
    provider {
      
      authenticate { auth -> runBlocking(Dispatchers.IO){
        val authHeader = auth.call.request.headers["Authorization"]
        if (authHeader==null){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = AuthErrorCode.NO_AUTHORIZATION_HEADER.name
            val msg = AuthErrorCode.NO_AUTHORIZATION_HEADER.msg
          })
          return@runBlocking
        }
        if (!authHeader.startsWith("Bearer ")){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = AuthErrorCode.AUTHORIZATION_HEADER_WRONG_FORMAT.name
            val msg = AuthErrorCode.AUTHORIZATION_HEADER_WRONG_FORMAT.msg
          })
          return@runBlocking
        }
        val accessToken = authHeader.substring("Bearer ".length)
        if (accessToken.isEmpty()){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = AuthErrorCode.EMPTY_TOKEN.name
            val msg = AuthErrorCode.EMPTY_TOKEN.msg
          })
          return@runBlocking
        }
        
        val verifier = JwtService.accessTokenVerifier
        val decodedJwt = try { verifier.verify(accessToken) }
        // Token was encoded by wrong algorithm. Required HMAC256.
        catch (ex: AlgorithmMismatchException){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = TokenError.TOKEN_ALGORITHM_MISMATCH.name
            val msg = TokenError.TOKEN_ALGORITHM_MISMATCH.msg
          })
          return@runBlocking
        }
        // Damaged Token - Токен повреждён и не может быть декодирован
        catch (ex: JWTDecodeException){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = TokenError.TOKEN_DAMAGED.name
            val msg = TokenError.TOKEN_DAMAGED.msg
          })
          return@runBlocking
        }
        // Modified Token - Токен умышленно модифицирован (подделан)
        catch (ex: SignatureVerificationException){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = TokenError.TOKEN_MODIFIED.name
            val msg = TokenError.TOKEN_MODIFIED.msg
          })
          return@runBlocking
        }
        // Token has expired
        catch (ex: TokenExpiredException){
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = TokenError.TOKEN_EXPIRED.name
            val msg = TokenError.TOKEN_EXPIRED.msg
          })
          return@runBlocking
        }
        // Common Verification Exception
        catch (ex: JWTVerificationException) {
          ex.printStackTrace()
          auth.call.respond(HttpStatusCode.Unauthorized, object {
            val code = TokenError.UNKNOWN_VERIFICATION_ERROR.name
            val msg = TokenError.UNKNOWN_VERIFICATION_ERROR.msg
          })
          return@runBlocking
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
