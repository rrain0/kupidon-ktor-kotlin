package com.rrain.kupidon.service

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.sessions.UserLiveStatusService
import com.rrain.util.`date-time`.now
import com.rrain.util.loop.retryUntil
import com.rrain.util.uuid.randomUuid
import java.util.UUID




// TODO
//  1) Сделать позже - save refresh token & device info to db as opened session
//  2) При генерации новых токенов старые блочить?
//     Этого можно достигнуть сравнением sessionExpiresAt из токена.

object JwtLoginService {
  
  fun login(
    userId: UUID,
    userRoles: Set<Role>,
    prevSessionId: UUID? = null
  ): SessionData {
    val sessionId = prevSessionId ?: retryUntil(
      { randomUuid() },
      { UserLiveStatusService.sessionToUser.putIfAbsent(it, userId) == null }
    )
    val now = now()
    val accessToken = JwtService.newAccessToken(
      userId.toString(), userRoles, sessionId.toString(), now
    )
    val refreshToken = JwtService.newRefreshToken(
      userId.toString(), sessionId.toString(), now
    )
    UserLiveStatusService
      .userOrCreate(userId)
      .sessionOrCreate(sessionId, refreshToken.expiresAt)
    // TODO check & remove expired sessions
    return SessionData(sessionId, accessToken.token, refreshToken.token)
  }
  
}



data class SessionData(
  val sessionId: UUID,
  val accessToken: String,
  val refreshToken: String,
)