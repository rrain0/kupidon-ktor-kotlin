package com.rrain.kupidon.routes.`exception-handling`

import com.rrain.kupidon.plugins.AuthenticationException
import com.rrain.kupidon.plugins.AuthorizationException
import com.rrain.kupidon.routes.NoSuchUserException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*



/*
  {
    code: ... for machines,
    msg: ... for humans,
    ... additional fields for machines if necessary,
    extraCode?: ... for machines,
  }

 */


fun Application.configureExceptionHandling(){
  
  install(StatusPages) {
    
    // Configure responses by exceptions
    exception<Throwable> { call, cause ->
      when (cause){
        is AuthenticationException -> call.respond(
          HttpStatusCode.Unauthorized,
          object {
            val code = 401
            //val httpError = "401 Unauthorized"
            val msg = cause.message
          }
        )
        
        is AuthorizationException -> call.respond(
          HttpStatusCode.Forbidden,
          object {
            val code = 403
            //val httpError = "403 Forbidden"
            val insufficientRoles = cause.insufficientRoles
            val msg = cause.message
          }
        )
        
        is NoSuchUserException -> call.respond(
          HttpStatusCode.BadRequest,
          object {
            val code = "incorrect-login-pwd"
            //val httpError = "400 Bad Request"
            val msg = cause.message
          }
        )
        
        else -> {
          call.respondText(text = "500 Server Error: $cause" , status = HttpStatusCode.InternalServerError)
          System.err.print(cause.printStackTrace())
        }
      }
    }
    
    /*
    // Configure responses by status codes
    status(HttpStatusCode.NotFound) { call, status ->
      call.respondText(text = "404: Page Not Found", status = status)
    }
    */
    
    /*
    // Send an html page respectively to status code
    // '#' will be replaced by numeric status code ("error#.html" -> "error404.html")
    // pages can be places in resources/ directory
    install(StatusPages) {
      statusFile(HttpStatusCode.Unauthorized, HttpStatusCode.PaymentRequired, filePattern = "error#.html")
    }
     */
    
  }
  
  
}