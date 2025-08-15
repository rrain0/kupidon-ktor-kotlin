package com.rrain.kupidon.route.routes.app.api.v1.auth

import com.rrain.kupidon.model.db.UserDataType
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.env.Env
import com.rrain.kupidon.service.jwt.JwtService
import com.rrain.kupidon.service.login.JwtLoginService
import com.rrain.kupidon.service.mongo.findUserById
import com.rrain.util.base.uuid.toUuid
import com.rrain.util.ktor.call.host
import com.rrain.util.ktor.call.port
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing



fun Application.addAuthLoginTestUserRoute() {
  routing {
    
    post(ApiV1Routes.authLoginTestUser) {
      val userId = when {
        Env.isDevelopment -> "795415da-a2cb-435b-80ee-98af28b3f0d0"
        else -> "795415da-a2cb-435b-80ee-98af28b3f0d0"
      }.toUuid()
      
      val user = findUserById(userId)
      
      if (user == null) {
        return@post call.respondBadRequest(
          code = "NO_USER",
          msg = "There is no user with such id",
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
