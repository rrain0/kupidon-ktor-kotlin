package com.rrain.kupidon.routes.routes.http.`app-api-v1`.auth

import com.rrain.kupidon.models.db.UserDataType
import com.rrain.kupidon.routes.`response-errors`.respondNotFound
import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.services.env.Env
import com.rrain.kupidon.services.jwt.JwtService
import com.rrain.kupidon.services.login.JwtLoginService
import com.rrain.kupidon.services.mongo.findUserById
import com.rrain.utils.base.uuid.toUuid
import com.rrain.utils.ktor.call.host
import com.rrain.utils.ktor.call.port
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing



fun Application.addRoutePostAuthLoginTestUser() {
  routing {
    
    post(ApiV1Routes.authLoginTestUser) {
      val userId = when {
        Env.isDev -> "795415da-a2cb-435b-80ee-98af28b3f0d0"
        Env.isProd -> "795415da-a2cb-435b-80ee-98af28b3f0d0"
        else -> throw RuntimeException("No test user for current app mode: ${Env.appMode}")
      }.toUuid()
      
      val user = findUserById(userId)
      
      user ?: return@post call.respondNotFound(
        code = "NO_USER",
        msg = "There is no user with such id",
      )
      
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
