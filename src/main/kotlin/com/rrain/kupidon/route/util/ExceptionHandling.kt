package com.rrain.kupidon.route.util

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
    
    // Configure responses from exceptions
    exception<Throwable> { call, cause ->
      when (cause){
        /*is AuthenticationException -> call.respond(
          HttpStatusCode.Unauthorized,
          object {
            val code = cause.code
            val msg = cause.message
          }
        )*/
        
        /*is AuthorizationException -> call.respond(
          HttpStatusCode.Forbidden,
          object {
            val code = 403
            val insufficientRoles = cause.insufficientRoles
            val msg = cause.message
          }
        )*/
        
        /*is NoSuchUserException -> call.respond(
          HttpStatusCode.BadRequest,
          object {
            val code = "incorrect-login-pwd"
            val msg = cause.message
          }
        )*/
        
        else -> {
          call.respondText(
            status = HttpStatusCode.InternalServerError,
            text = "500 Internal Server Error: $cause",
          )
          cause.printStackTrace()
        }
      }
    }
    
    /*
    // Configure responses from status codes
    status(HttpStatusCode.NotFound) { call, status ->
      call.respondText(text = "404: Page Not Found", status = status)
    }
    */
    
    /*
    // Send an html page respectively to status code
    // '#' will be replaced by numeric status code ("error#.html" -> "error404.html")
    // pages can be places in resources/ directory
    install(StatusPages) {
      statusFile(
        HttpStatusCode.Unauthorized, HttpStatusCode.PaymentRequired,
        filePattern = "error#.html"
      )
    }
     */
    
  }
  
  
}