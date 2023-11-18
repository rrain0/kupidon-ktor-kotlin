package com.rrain.kupidon.service.lang

import org.intellij.lang.annotations.Language






data class EmailInitialVerificationParams(
  val userName: String,
  val verificationUrl: String,
)
val EmailInitialVerificationUiTemplateText = listOf(
  UiTemplateText(
    value = "EmailInitialVerification",
    lang = "en-US",
    text = { params: EmailInitialVerificationParams ->
      @Language("html") val html = """
        <html lang="ru">
        <head>
          <meta charset="utf-8">
          <title>Email verification in the Kupidon application</title>
        </head>
        <body>
          <p>${params.userName}, welcome to the Kupidon app!</p>
          <p>To verify your email address, just follow the link:</p>
          <p><a href="${params.verificationUrl}">${params.verificationUrl}</a></p>
          <p>The link is valid for 1 day.</p>
        </body>
        </html>
      """.trimIndent()
      html
    },
  ),
  UiTemplateText(
    value = "EmailInitialVerification",
    lang = "ru-RU",
    text = { params: EmailInitialVerificationParams ->
      @Language("html") val html = """
        <html lang="ru">
        <head>
          <meta charset="utf-8">
          <title>Верификация почты в приложении Купидон</title>
        </head>
        <body>
          <p>${params.userName}, добро пожаловать в приложение Купидон!</p>
          <p>Для того, чтобы подтвердить свой адрес электронной почты, просто перейдите по ссылке:</p>
          <p><a href="${params.verificationUrl}">${params.verificationUrl}</a></p>
          <p>Ссылка действительна 1 сутки.</p>
        </body>
        </html>
      """.trimIndent()
      html
    },
  ),
)
