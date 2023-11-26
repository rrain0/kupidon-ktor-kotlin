package com.rrain.kupidon.service.lang.`ui-value`

import com.rrain.kupidon.service.lang.AppLang
import com.rrain.kupidon.service.lang.UiTemplateText
import com.rrain.kupidon.service.lang.UiText
import org.intellij.lang.annotations.Language






object EmailInitialVerificationUiText {
  
  val emailTitle = listOf(
    UiText(
      value = "EmailInitialVerificationEmailTitle",
      lang = AppLang.enUS.value,
      text = "Kupidon - email verification",
    ),
    UiText(
      value = "EmailInitialVerificationEmailTitle",
      lang = AppLang.ruRU.value,
      text = "Купидон - верификация почты",
    ),
  )
  
  data class EmailContentParams(
    val userName: String,
    val verificationUrl: String,
  )
  val emailContent = listOf(
    UiTemplateText(
      value = "EmailInitialVerificationEmailContent",
      lang = AppLang.enUS.value,
      text = { params: EmailContentParams ->
        @Language("html") val html = """
          <html lang="${AppLang.enUS.value}">
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
    UiTemplateText(
      value = "EmailInitialVerificationEmailContent",
      lang = AppLang.ruRU.value,
      text = { params: EmailContentParams ->
        @Language("html") val html = """
          <html lang="${AppLang.ruRU.value}">
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
    ),
  )
  
}


/*data class EmailInitialVerificationParams(
  val userName: String,
  val verificationUrl: String,
)*/

