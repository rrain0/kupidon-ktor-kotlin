package com.rrain.kupidon.service

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.sessions.SessionsService
import com.rrain.kupidon.service.sessions.UserSession
import com.rrain.kupidon.service.sessions.UserSessions
import com.rrain.util.`date-time`.now
import com.rrain.util.loop.retryUntil
import com.rrain.util.uuid.randomUuid
import java.util.UUID




// TODO
//  1) Сделать позже - save refresh token & device info to db as opened session
//  2) При генерации access token генерится и новый refresh token,
//  а старые рефреши всё ещё валидны

object JwtLoginService {
  
  fun login(
    userId: UUID,
    userRoles: Set<Role>,
    prevSessionId: UUID? = null
  ): SessionData {
    val sessionId = prevSessionId ?: retryUntil(
      { randomUuid() },
      { SessionsService.sessions.add(it) }
    )
    val now = now()
    val accessToken = JwtService.newAccessToken(
      userId.toString(), userRoles, sessionId.toString(), now
    )
    val refreshToken = JwtService.newRefreshToken(
      userId.toString(), sessionId.toString(), now
    )
    SessionsService.userSessions
      .getOrPut(sessionId) { UserSessions(userId, now) }
      .apply {
        lastStartOnlineAt = now
        // Clear expired sessions
        sessions.values.removeIf { (it.expiresAt - now).inWholeMilliseconds <= 0 }
        sessions.getOrPut(sessionId) { UserSession(userId, now, refreshToken.expiresAt) }
        onlineStatusSubscribers.forEach {
          // TODO online - notify user became online
        }
      }
    return SessionData(sessionId, accessToken.token, refreshToken.token)
  }
  
}



data class SessionData(
  val sessionId: UUID,
  val accessToken: String,
  val refreshToken: String,
)