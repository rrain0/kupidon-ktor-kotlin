package com.rrain.kupidon.routes.routes.http.test

import com.rrain.kupidon.services.email.EmailMessage
import com.rrain.kupidon.services.email.EmailService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addSendEmailTestRoutes() {
  routing {
    
    data class EmailRequest(val email: String?)
    get("/test/send-email") {
      EmailService.sendEmail(EmailMessage(
        fromName = "Kupidon",
        to = call.receive<EmailRequest>().email!!,
        title = "Test Email",
        content = "test email",
      ))
      call.respond(HttpStatusCode.OK)
    }
    
  }
}