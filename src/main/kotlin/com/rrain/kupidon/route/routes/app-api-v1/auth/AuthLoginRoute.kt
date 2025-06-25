package com.rrain.kupidon.route.routes.app.api.v1.auth

import com.mongodb.client.model.Filters
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashService
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.kupidon.service.mongo.model.UserProfilePhotoM
import com.rrain.kupidon.service.mongo.model.projectionUserM
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import io.ktor.server.application.*
import io.ktor.server.plugins.*
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
      val login =
        try { call.receive<LoginBodyIn>() }
        catch (ex: Exception) { return@post call.respondInvalidBody() }
      
      val nUserEmail = UserM::email.name
      val nUserPhotos = UserM::photos.name
      val nPhotoBinData = UserProfilePhotoM::binData.name
      
      val user = collUsers
        .find(Filters.eq(nUserEmail, login.login))
        .projectionUserM()
        .firstOrNull()
      
      if (user == null || !PwdHashService.checkPwd(login.pwd, user.pwd)) {
        return@post call.respondBadRequest(
          code = "NO_USER",
          msg = "There is no user with such pair login-password",
        )
      }
      
      val id = user.id.toString()
      val roles = user.roles
      
      val domain = call.request.origin.serverHost
      
      val accessToken = JwtService.generateAccessToken(id, roles)
      val refreshToken = JwtService.generateRefreshToken(id)
      
      // сделать позже save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(refreshToken,domain)
      )
      call.respond(mapOf(
        "accessToken" to accessToken,
        "user" to user.toApi(UserDataType.Current, call.host, call.port)
      ))
    }
  }
}
