package com.rrain.kupidon.service.lang.`ui-value`

import com.rrain.kupidon.service.lang.`lang-service`.Lang
import com.rrain.kupidon.service.lang.`lang-service`.UiText
import com.rrain.kupidon.service.lang.`lang-service`.UiTemplate
import org.intellij.lang.annotations.Language



object EmailInitialVerificationUiText {
  
  val emailTitle = listOf(
    UiText(
      lang = Lang.enUS,
      value = "Kupidon - email verification",
    ),
    UiText(
      lang = Lang.ruRU,
      value = "Купидон - верификация почты",
    ),
  )
  
  data class EmailContentParams(
    val userName: String,
    val verificationUrl: String,
  )
  val emailContent = listOf(
    UiTemplate(
      lang = Lang.enUS,
      value = { params: EmailContentParams ->
        @Language("html") val html = """
          <html lang="${Lang.enUS.value}">
          <head>
            <meta charset="utf-8">
            <title>Email verification in the Kupidon application</title>
          </head>
          <body>
            <p>${params.userName}, welcome to the Kupidon app!</p>
            <p>To verify your email address, just follow the link:</p>
            <p><a href="${params.verificationUrl}">${params.verificationUrl}</a></p>
            <p>The link is valid for 1 day.</p>
            <p>Otherwise, log into the application and request a new one or change your email.</p>
          </body>
          </html>
        """.trimIndent()
        html
      },
    ),
    UiTemplate(
      value = { params: EmailContentParams ->
        @Language("html") val html = """
          <html lang="${Lang.ruRU.value}">
          <head>
            <meta charset="utf-8">
            <title>Верификация почты в приложении Купидон</title>
          </head>
          <body>
            <p>${params.userName}, добро пожаловать в приложение Купидон!</p>
            <p>Для того, чтобы подтвердить свой адрес электронной почты, просто перейдите по ссылке:</p>
            <p><a href="${params.verificationUrl}">${params.verificationUrl}</a></p>
            <p>Ссылка действительна 1 сутки.</p>
            <p>Иначе войдите в приложение и запросите новую или смените почту.</p>
          </body>
          </html>
        """.trimIndent()
        html
      },
      lang = Lang.ruRU,
    ),
  )
  
}



