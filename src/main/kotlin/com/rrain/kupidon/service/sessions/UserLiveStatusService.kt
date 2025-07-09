package com.rrain.kupidon.service.sessions

import com.rrain.util.any.maxOf
import com.rrain.util.`date-time`.isExpired
import com.rrain.util.`date-time`.now
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap




typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession


object UserLiveStatusService {
  val user: MutableMap<UserId, UserStatus> = ConcurrentHashMap()
  val sessionToUser: MutableMap<SessionId, UserId> = ConcurrentHashMap()
  val sessionToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = ConcurrentHashMap()
  val wsSessionToSessions: MutableMap<WsSession, MutableSet<SessionId>> = ConcurrentHashMap()
  val sessionToWatchedUsers: MutableMap<SessionId, MutableSet<UserId>> = ConcurrentHashMap()
  
  fun userOrCreate(userId: UserId) = (
    user.getOrPut(userId) { UserStatus(userId) }
  )
  
  fun onlineUserSession(
    userId: UserId,
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
  ): UserStatus {
    val now = now()
    return userOrCreate(userId)
      .apply { onlineSession(sessionId, expiresAt, wsSession, now) }
  }
  
  fun offlineUserSession(userId: UUID, sessionId: UUID): UserStatus? {
    val now = now()
    val user = user[userId]?.apply { offlineSession(sessionId, now) }
    return user
  }
  
  fun offlineWs(wsSession: WsSession): List<UserStatus> {
    val now = now()
    val users: MutableList<UserStatus> = mutableListOf()
    wsSessionToSessions.remove(wsSession)?.forEach { sessionId ->
      sessionToUser[sessionId]?.also { userId ->
        user[userId]?.apply {
          offlineWs(sessionId, wsSession, now)
          users += this
        }
      }
      sessionToWsSessions.computeIfPresent(sessionId) { _, v ->
        v -= wsSession
        v.ifEmpty { null }
      }
    }
    return users
  }
  
  fun replaceWatched(sessionId: SessionId, watchedUserIds: Set<UserId>) {
    sessionToWatchedUsers.getOrPut(sessionId) { ConcurrentHashMap.newKeySet() }.let {
      val watch = watchedUserIds - it
      val unwatch = it - watchedUserIds
      watch.map { userOrCreate(it) }.forEach { it.watchers += sessionId }
      unwatch.mapNotNull { user[it] }.forEach { it.watchers -= sessionId }
      it.removeAll(unwatch)
    }
  }
}


data class UserStatus(
  val id: UserId,
  @Volatile var lastStartOnlineAt: Instant? = null,
) {
  val sessions: MutableMap<SessionId, SessionStatus> = ConcurrentHashMap()
  val watchers: MutableSet<SessionId> = ConcurrentHashMap.newKeySet()
  
  fun sessionOrCreate(sessionId: SessionId, expiresAt: Instant? = null) = (
    sessions.getOrPut(sessionId) { SessionStatus(sessionId, expiresAt) }
      .also { UserLiveStatusService.sessionToUser[it.id] = id }
  )
  
  
  fun online(now: Instant? = now()) = sessions.values.any {
    lastStartOnlineAt = maxOf(it.lastStartOnlineAt, lastStartOnlineAt)
    it.online(now)
  }
  val empty get() = sessions.isEmpty() && watchers.isEmpty()
  
  
  fun onlineSession(
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant? = now(),
  ) {
    lastStartOnlineAt = now
    sessionOrCreate(sessionId, expiresAt).onlineSession(expiresAt, wsSession, now)
  }
  
  fun offlineSession(sessionId: SessionId, now: Instant? = null) {
    sessions[sessionId]?.offlineSession(now)
    online(now)
    removeIfEmpty()
  }
  
  fun offlineWs(sessionId: SessionId, wsSession: WsSession, now: Instant? = null) {
    sessions[sessionId]?.offlineWs(wsSession, now)
    online(now)
    removeIfEmpty()
  }
  
  
  fun removeIfEmpty() {
    UserLiveStatusService.user.computeIfPresent(id) { _, it -> if (it.empty) null else it }
  }
}


data class SessionStatus(
  val id: SessionId,
  @Volatile var expiresAt: Instant? = null,
  @Volatile var lastStartOnlineAt: Instant? = null,
  @Volatile var lastIsOnline: Boolean = false,
) {
  val wsSessions: MutableSet<WsSession> = ConcurrentHashMap.newKeySet()
  
  
  fun online(now: Instant? = now()) = lastIsOnline && !(expiresAt?.isExpired() ?: true) && wsSessions.isNotEmpty()
    .also {
      if (it) lastStartOnlineAt = maxOf(now, lastStartOnlineAt)
      if (!it) lastIsOnline = false
    }
  val empty get() = !online() && wsSessions.isEmpty()
  
  
  fun onlineSession(
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant? = now(),
  ) {
    this.expiresAt = expiresAt
    lastStartOnlineAt = now
    lastIsOnline = true
    addWs(wsSession)
  }
  
  fun addWs(wsSession: WsSession) {
    wsSessions += wsSession.apply {
      UserLiveStatusService.sessionToWsSessions.getOrPut(id) { ConcurrentHashMap.newKeySet() } += this
      UserLiveStatusService.wsSessionToSessions.getOrPut(this) { ConcurrentHashMap.newKeySet() } += id
    }
  }
  
  fun offlineSession(now: Instant? = null) {
    online(now)
    lastIsOnline = false
    removeIfEmpty()
  }
  
  fun offlineWs(wsSession: WsSession, now: Instant? = null) {
    online(now)
    wsSessions -= wsSession
    online(now)
    removeIfEmpty()
  }
  
  
  fun removeIfEmpty() {
    if (empty) {
      UserLiveStatusService.sessionToUser.remove(id)
        ?.let { UserLiveStatusService.user[it] }?.sessions?.remove(id)
      UserLiveStatusService.sessionToWatchedUsers.remove(id)
      UserLiveStatusService.sessionToWsSessions.remove(id)?.forEach {
        UserLiveStatusService.wsSessionToSessions.computeIfPresent(it) { _, v ->
          v -= id
          v.ifEmpty { null }
        }
      }
    }
  }
}