package com.rrain.kupidon.route.routes.app.api.v1.auth

import com.mongodb.client.model.Filters
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashService
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.model.db.UserDataType
import com.rrain.kupidon.model.db.UserM
import com.rrain.kupidon.model.db.projectionUserM
import com.rrain.kupidon.service.JwtLoginService
import com.rrain.util.ktor.call.host
import com.rrain.util.ktor.call.port
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull



fun Application.addAuthLoginRoute() {
  routing {
    
    data class LoginBodyIn(
      val login: String,
      val pwd: String
    )
    
    post(ApiV1Routes.authLogin) {
      val loginIn =
        try { call.receive<LoginBodyIn>() }
        catch (ex: Exception) { return@post call.respondInvalidBody() }
      
      val user = collUsers
        .find(Filters.eq(UserM::email.name, loginIn.login))
        .projectionUserM()
        .firstOrNull()
      
      if (user == null || !PwdHashService.checkPwd(loginIn.pwd, user.pwd)) {
        return@post call.respondBadRequest(
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
