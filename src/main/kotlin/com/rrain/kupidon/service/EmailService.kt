package com.rrain.kupidon.service

import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail
import java.nio.charset.StandardCharsets



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
  
  
  // https://commons.apache.org/proper/commons-email/userguide.html
  
  
  fun sendEmail(emailMessage: EmailMessage){
    SimpleEmail().run {
      hostName = "smtp.yandex.ru"
      setSmtpPort(465)
      setAuthenticator(DefaultAuthenticator(fromEmail, fromPwd))
      isSSLOnConnect = true
      setFrom(fromEmail)
      addTo(emailMessage.to)
      subject = emailMessage.title
      setMsg(emailMessage.content)
      send()
    }
  }
  
  
  fun sendHtmlEmail(emailMessage: EmailMessage){
    HtmlEmail().run {
      hostName = "smtp.yandex.ru"
      setSmtpPort(465)
      setAuthenticator(DefaultAuthenticator(fromEmail, fromPwd))
      isSSLOnConnect = true
      setFrom(fromEmail, emailMessage.fromName)
      addTo(emailMessage.to)
      subject = emailMessage.title
      setCharset(StandardCharsets.UTF_8.name())
      setHtmlMsg(emailMessage.content)
      send()
    }
  }
  
}

data class EmailMessage(
  val fromName: String, // Отображаемое имя рядом с почтой отправителя
  val to: String, // получатель
  val title: String, // тема письма
  val content: String, // тело письма
)