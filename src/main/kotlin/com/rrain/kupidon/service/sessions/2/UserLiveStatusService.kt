package com.rrain.kupidon.service.sessions.`2`

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID



val updatedUsers: MutableSet<UserState> = mutableSetOf()
val updatedSessions: MutableSet<SessionState> = mutableSetOf()



typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession


object UserLiveStatusService {
  
  @Synchronized fun <T> use(block: UserLiveStatusService.() -> T) = block(UserLiveStatusService)
  
  
  private val userIdToUser: MutableMap<UserId, UserState> = mutableMapOf()
  private val userIdToSessionIds: MutableMap<UserId, MutableSet<SessionId>> = mutableMapOf()
  
  private val sessionIdToSession: MutableMap<SessionId, SessionState> = mutableMapOf()
  private val sessionIdToUserId: MutableMap<SessionId, UserId> = mutableMapOf()
  private val sessionIdToWatchedUserIds: MutableMap<SessionId, MutableSet<UserId>> = mutableMapOf()
  private val sessionIdToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = mutableMapOf()
  
  private val wsSessionToSessionIds: MutableMap<WsSession, MutableSet<SessionId>> = mutableMapOf()
  
  
  object Users {
    fun get(id: UserId) = userIdToUser[id]
    fun getOrAdd(user: UserState) = userIdToUser.getOrPut(user.id) {
      updatedUsers += user
      user
    }
  }
  
  object Sessions {
    fun get(id: SessionId) = sessionIdToSession[id]
    fun getOrAdd(session: SessionState) = sessionIdToSession.getOrPut(session.id) {
      updatedSessions += session
      session
    }
    fun update(session: SessionState) = sessionIdToSession.computeIfPresent(session.id) { _, currSession ->
      session
      // TODO notify about updates
    }
    
    object ToWsSessions {
      fun get(sessionId: SessionId) = sessionIdToWsSessions[sessionId]
      fun add(sessionId: SessionId, wsSession: WsSession) {
        sessionIdToWsSessions.getOrPut(sessionId) { mutableSetOf() }.add(wsSession)
        WsSessions.ToSessions.add(wsSession, sessionId)
      }
    }
  }
  
  
  object WsSessions {
    fun remove(wsSession: WsSession) = ToSessions.remove(wsSession)
    
    object ToSessions {
      fun get(wsSession: WsSession) = wsSessionToSessionIds[wsSession]
      fun add(wsSession: WsSession, sessionId: SessionId) {
        wsSessionToSessionIds.getOrPut(wsSession) { mutableSetOf() }.add(sessionId)
      }
      fun remove(wsSession: WsSession) {
        wsSessionToSessionIds.remove(wsSession)?.forEach {
          sessionIdToWsSessions[it]?.remove(wsSession)
        }
      }
    }
  }
  
}



data class UserState(
  val id: SessionId,
  val expiresAt: Instant,
  val wasOnlineAt: Instant? = null,
  val online: Boolean = false,
)
data class SessionState(
  val id: SessionId,
  val expiresAt: Instant,
  val wasOnlineAt: Instant? = null,
  val online: Boolean = false,
)







