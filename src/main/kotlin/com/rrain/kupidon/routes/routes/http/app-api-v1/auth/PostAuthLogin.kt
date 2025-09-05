package com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth

import com.rrain.kupidon.services.jwt.JwtService
import com.rrain.kupidon.services.`pwd-hash`.PwdHashService
import com.rrain.kupidon.routes.`response-errors`.respondInvalidBody
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.services.login.JwtLoginService
import com.rrain.kupidon.services.mongo.findUserByEmail
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.addRoutePostAuthLogin() {
  routing {
    
    data class LoginBodyIn(
      val login: String,
      val pwd: String
    )
    
    post(ApiV1Routes.authLogin) {
      val loginIn =
        try { call.receive<LoginBodyIn>() }
        catch (ex: Exception) { return@post call.respondInvalidBody() }
      
      val user = findUserByEmail(loginIn.login)
      
      if (user == null || !PwdHashService.checkPwd(loginIn.pwd, user.pwd)) {
        return@post call.respondNotFound(
          code = "NO_USER",
          msg = "There is no user with such login-password pair",
        )
      }
      
      val newSession = JwtLoginService.login(user.id, user.roles)
      
      call.response.cookies.append(
        JwtService.getRefreshTokenCookie(newSession.refreshToken, call.host)
      )
      call.respond(mapOf(
        "accessToken" to newSession.accessToken,
        "user" to user.toApi(UserDataType.Current, call.host, call.port)
      ))
    }
  }
}
