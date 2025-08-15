package com.rrain.kupidon.service.login

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.jwt.JwtService
import com.rrain.util.base.`date-time`.now
import com.rrain.util.base.uuid.randomUuid
import java.util.UUID




// TODO
//  1) Сделать позже - save refresh token & device info to db as opened session
//  2) При генерации новых токенов старые блочить?
//     Этого можно достигнуть сравнением sessionExpiresAt из токена.

object JwtLoginService {
  
  fun login(
    userId: UUID,
    userRoles: Set<Role>,
    currSessionId: UUID? = null
  ): SessionData {
    val sessionId = currSessionId ?: randomUuid()
    val now = now()
    val accessToken = JwtService.newAccessToken(userId, userRoles, sessionId, now)
    val refreshToken = JwtService.newRefreshToken(userId, sessionId, now)
    return SessionData(sessionId, accessToken.token, refreshToken.token)
  }
  
}



data class SessionData(
  val sessionId: UUID,
  val accessToken: String,
  val refreshToken: String,
)