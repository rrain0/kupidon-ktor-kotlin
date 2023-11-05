package com.rrain.kupidon.service

import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.util.extension.get
import io.ktor.server.application.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail
import org.intellij.lang.annotations.Language
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
  
  
  fun sendEmail(emailMessage: EmailMessage){
    val email = SimpleEmail()
    email.hostName = "smtp.yandex.ru"
    email.setSmtpPort(465)
    email.setAuthenticator(DefaultAuthenticator(fromEmail, fromPwd))
    email.isSSLOnConnect = true
    email.setFrom(fromEmail)
    email.addTo(emailMessage.to)
    email.subject = emailMessage.subject
    email.setMsg(emailMessage.body)
    email.send()
  }
  
  
  // https://commons.apache.org/proper/commons-email/userguide.html
  fun sendVerificationEmail(user: User, verificationUrl: String) {
    // todo Ссылка действительна 1 сутки, иначе войдите в приложение и запросите новую или смените почту.
    
    @Language("html") val html = """
      <html lang="ru">
      <head>
        <meta charset="utf-8">
        <title>Верификация почты в приложении Купидон</title>
      </head>
      <body>
        <p>${user.name}, добро пожаловать в приложение Купидон!</p>
        <p>Для того, чтобы подтвердить свой адрес электронной почты, просто перейдите по ссылке:</p>
        <p><a href="$verificationUrl">$verificationUrl</a></p>
        <p>Ссылка действительна 1 сутки.</p>
      </body>
      </html>
    """.trimIndent()
    
    HtmlEmail().run {
      hostName = "smtp.yandex.ru"
      setSmtpPort(465)
      setAuthenticator(DefaultAuthenticator(fromEmail, fromPwd))
      isSSLOnConnect = true
      setFrom(fromEmail, "Купидон")
      addTo(user.email!!)
      subject = "Купидон - верификация"
      setCharset(StandardCharsets.UTF_8.name())
      setHtmlMsg(html)
      send()
    }
    
  }
  
}

data class EmailMessage(
  val to: String, // получатель
  val subject: String, // тема письма
  val body: String, // тело письма
)