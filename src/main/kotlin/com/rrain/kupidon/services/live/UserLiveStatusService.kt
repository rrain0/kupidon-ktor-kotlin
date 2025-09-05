package com.rrain.kupidon.services.live

import com.rrain.utils.base.comparison.maxOf
import com.rrain.utils.base.collections.concurrentMapOf
import com.rrain.utils.base.collections.concurrentSetOf
import com.rrain.utils.base.`date-time`.isExpired
import com.rrain.utils.base.`date-time`.now
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID




typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession




// Добавление сущностей идёт в порядке сверху вниз: user -> session -> wsSession.
// Изменение / удаление сущностей идёт в прядке снизу вверх: wsSession -> session -> user.
// При добавлении сущности сначала по порядку досоздаются все верхние сущности.
// При изменении сущности изменения должны сразу распространяться вверх и проверяться, нужно ли её удалить.
// При удалении сущности верхние сущности должны сразу проверяться на то, могут ли быть удалены и удаляться.


private val userStatus: MutableMap<UserId, UserStatus> = concurrentMapOf()
private val onlineUsers: MutableSet<UserId> = concurrentSetOf()
private val sessionToUser: MutableMap<SessionId, UserId> = concurrentMapOf()
private val sessionToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()
private val sessionToWatchedUsers: MutableMap<SessionId, MutableSet<UserId>> = concurrentMapOf()
private val wsSessionToSessions: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()



object UserLiveStatusService {
  
  @Synchronized fun <T> use(block: (service: UserLiveStatusService) -> T) = (
    block(UserLiveStatusService)
  )
  
  
  fun getUser(id: UserId) = userStatus[id]
  fun isUserOnline(id: UserId) = getUser(id)?.online ?: false
  
  fun getOrAddUser(userToAdd: UserStatus) = userStatus.getOrPut(userToAdd.id) { userToAdd }
  
  fun getUserBySession(id: SessionId) = sessionToUser[id]?.let { userStatus[it] }
  fun getWsSessionBySession(id: SessionId) = sessionToWsSessions[id]
  
  
  fun userOrAdd(userId: UserId) = getOrAddUser(UserStatus(userId))
  
  
  fun onlineUserSession(
    userId: UserId,
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant = now(),
  ): UserStatus {
    return userOrAdd(userId)
      .apply {
        getOrAddSession(sessionId, expiresAt, wsSession)
          .onlineSession(expiresAt, wsSession, now)
      }
  }
  
  fun offlineUserSession(
    userId: UserId,
    sessionId: SessionId,
    now: Instant = now(),
  ): UserStatus? {
    return userStatus[userId]?.apply {
      sessions[sessionId]?.offlineSession(now)
    }
  }
  
  fun offlineWs(
    wsSession: WsSession,
    now: Instant = now(),
  ): List<UserStatus> {
    val users: MutableList<UserStatus> = mutableListOf()
    wsSessionToSessions.remove(wsSession)?.forEach { sessionId ->
      Serv.getUserBySession(sessionId)?.apply {
        sessions[sessionId]?.apply {
          updateStatus(now)
          wsSessions -= wsSession
          updateStatus(now)
          removeIfEmpty()
        }
        users += this
      }
      sessionToWsSessions.computeIfPresent(sessionId) { _, wsSessions ->
        wsSessions.apply { remove(wsSession) }.ifEmpty { null }
      }
    }
    return users
  }
  
  fun replaceWatched(sessionId: SessionId, watchedUserIds: Set<UserId>) {
    sessionToWatchedUsers.compute(sessionId) { _, it -> (it ?: concurrentSetOf()).let {
      val watch = watchedUserIds - it
      val unwatch = it - watchedUserIds
      watch.map { userOrAdd(it) }.forEach { it.watchers += sessionId }
      unwatch.mapNotNull { userStatus[it] }.forEach { it.watchers -= sessionId }
      it.removeAll(unwatch)
      it.ifEmpty { null }
    } }
  }
}
private val Serv = UserLiveStatusService




data class UserStatus(
  val id: UserId,
  @Volatile var onlineAt: Instant? = null,
  @Volatile var online: Boolean = false,
) {
  val sessions: MutableMap<SessionId, SessionStatus> = concurrentMapOf()
  // Sessions that watch this user
  val watchers: MutableSet<SessionId> = concurrentSetOf()
  
  
  val empty get() = sessions.isEmpty() && watchers.isEmpty()
  
  fun updateStatus() {
    online = false
    sessions.values.forEach {
      online = online || it.online
      onlineAt = maxOf(it.onlineAt, onlineAt)
    }
    if (online) onlineUsers += id else onlineUsers -= id
  }
  
  
  fun getOrAddSession(
    sessionId: SessionId,
    expiresAt: Instant? = null,
    wsSession: WsSession? = null,
  ) = (
    sessions.getOrPut(sessionId) { SessionStatus(sessionId, expiresAt) }.also {
      sessionToUser[it.id] = id
      if (wsSession != null) it.getOrAddWsSession(wsSession)
    }
  )
  
  
  fun removeIfEmpty() {
    userStatus.computeIfPresent(id) { _, user ->
      if (user.empty) { onlineUsers -= id; null }
      user
    }
  }
}



data class SessionStatus(
  val id: SessionId,
  @Volatile var expiresAt: Instant? = null,
  @Volatile var onlineAt: Instant? = null,
  @Volatile var online: Boolean = false,
) {
  val wsSessions: MutableSet<WsSession> = concurrentSetOf()
  
  
  val empty get() = !online && wsSessions.isEmpty()
  
  fun updateStatus(now: Instant? = now()) {
    online = online && !(expiresAt?.isExpired() ?: true) && wsSessions.isNotEmpty()
    if (online) onlineAt = maxOf(now, onlineAt)
    Serv.getUserBySession(id)?.updateStatus()
  }
  
  
  fun getOrAddWsSession(wsSession: WsSession) = wsSession.apply {
    if (wsSessions.add(this)) {
      sessionToWsSessions.getOrPut(id) { concurrentSetOf() } += this
      wsSessionToSessions.getOrPut(this) { concurrentSetOf() } += id
    }
  }
  
  
  
  fun onlineSession(
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant? = now(),
  ) {
    this.expiresAt = expiresAt
    onlineAt = now
    online = true
    getOrAddWsSession(wsSession)
    updateStatus(now)
  }
  
  fun offlineSession(now: Instant? = null) {
    updateStatus(now)
    online = false
    updateStatus(now)
    removeIfEmpty()
  }
  
  
  fun removeIfEmpty() {
    if (empty) {
      Serv.getUserBySession(id)?.also { userStatus ->
        userStatus.sessions.remove(id)
        userStatus.removeIfEmpty()
      }
      sessionToUser.remove(id)
      sessionToWatchedUsers.remove(id)
      sessionToWsSessions.remove(id)?.also { wsSessions ->
        wsSessions.forEach {
          wsSessionToSessions.computeIfPresent(it) { _, sessions ->
            sessions.apply { remove(id) }.ifEmpty { null }
          }
        }
      }
    }
  }
}

