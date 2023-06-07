package com.rrain.kupidon.service

import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail


fun Application.configureEmailService(){
  
  val appConfig = environment.config
  
  EmailService.run {
    fromEmail = appConfig["mail.email"]
    fromPwd = appConfig["mail.pwd"]
  }
  
}



object EmailService {
  
  lateinit var fromEmail: String
  lateinit var fromPwd: String
  
  fun sendEmail(emailMessage: EmailMessage){
    val email = SimpleEmail()
    email.hostName = "smtp.yandex.ru"
    email.setSmtpPort(465)
    email.setAuthenticator(DefaultAuthenticator(fromEmail, fromPwd))
    email.isSSLOnConnect = true
    email.setFrom("dedkov.dmitriy.97@yandex.ru")
    email.addTo(emailMessage.to)
    email.subject = emailMessage.subject
    email.setMsg(emailMessage.body)
    email.send()
  }
  
}

data class EmailMessage(
  val to: String, // получатель
  val subject: String, // тема письма
  val body: String, // тело письма
)