package com.rrain.kupidon.service

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.sessions.SessionsService
import com.rrain.kupidon.service.sessions.UserSession
import com.rrain.kupidon.service.sessions.UserSessions
import com.rrain.util.`date-time`.isExpired
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
      { SessionsService.sessionToUser.putIfAbsent(it, userId) == null }
    )
    val now = now()
    val accessToken = JwtService.newAccessToken(
      userId.toString(), userRoles, sessionId.toString(), now
    )
    val refreshToken = JwtService.newRefreshToken(
      userId.toString(), sessionId.toString(), now
    )
    SessionsService.userToSessions
      .getOrPut(sessionId) { UserSessions(userId) }
      .apply {
        lastStartOnlineAt = now
        // Clear expired sessions of this user
        sessions.values.removeIf { it.expiresAt.isExpired(now) }
        sessions.getOrPut(sessionId) { UserSession(userId, refreshToken.expiresAt, now) }
      }
    return SessionData(sessionId, accessToken.token, refreshToken.token)
  }
  
}



data class SessionData(
  val sessionId: UUID,
  val accessToken: String,
  val refreshToken: String,
)