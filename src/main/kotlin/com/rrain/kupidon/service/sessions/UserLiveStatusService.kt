package com.rrain.kupidon.service.sessions

import com.rrain.util.any.maxOf
import com.rrain.util.collections.concurrentMapOf
import com.rrain.util.collections.concurrentSetOf
import com.rrain.util.`date-time`.isExpired
import com.rrain.util.`date-time`.now
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID




typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession



// Добавление сущностей идёд в порядке сверху вниз: user -> session -> wsSession.
// Изменение / удаление сущностей идёт в прядке сниуз вверх: wsSession -> session -> user.
// При добавлении сущности все сначала по порядку досоздаются все верхние сущности.
// При изменении сущности изменения должны сразу распространяться вверх и проверяться, нужно ли её удалить.
// При удалении сущности верхние сущности должны сразу проверяться на то, могут ли быть удалены и удаляться.


private val user: MutableMap<UserId, UserStatus> = concurrentMapOf()
private val sessionToUser: MutableMap<SessionId, UserId> = concurrentMapOf()
private val sessionToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()
private val sessionToWatchedUsers: MutableMap<SessionId, MutableSet<UserId>> = concurrentMapOf()
private val wsSessionToSessions: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()



object UserLiveStatusService {
  
  @Synchronized fun <T> use(block: (service: UserLiveStatusService) -> T) = block(UserLiveStatusService)
  
  
  fun getUser(id: UserId) = user[id]
  fun getOrAddUser(userToAdd: UserStatus) = user.getOrPut(userToAdd.id) { userToAdd }
  
  fun getUserBySession(id: SessionId) = sessionToUser[id]?.let { user[it] }
  fun getWsSessionBySession(id: SessionId) = sessionToWsSessions[id]
  
  
  fun userOrAdd(userId: UserId) = getOrAddUser(UserStatus(userId))
  
  
  fun onlineUserSession(
    userId: UserId,
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
  ): UserStatus {
    val now = now()
    return userOrAdd(userId)
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
    sessionToWatchedUsers.getOrPut(sessionId) { concurrentSetOf() }.let {
      val watch = watchedUserIds - it
      val unwatch = it - watchedUserIds
      watch.map { userOrAdd(it) }.forEach { it.watchers += sessionId }
      unwatch.mapNotNull { user[it] }.forEach { it.watchers -= sessionId }
      it.removeAll(unwatch)
    }
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
  
  fun updateStatus() = this.also {
    online = false
    sessions.values.forEach {
      online = online || it.online
      onlineAt = maxOf(it.onlineAt, onlineAt)
    }
  }
  
  
  fun getOrAddSession(
    sessionId: SessionId,
    expiresAt: Instant? = null,
    wsSession: WsSession? = null,
  ) = (
    sessions.getOrPut(sessionId) { SessionStatus(sessionId, expiresAt) }.also {
      sessionToUser[it.id] = id
      if (wsSession != null) it.addWs(wsSession)
    }
  )
  
  
  fun onlineSession(
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant? = now(),
  ) {
    onlineAt = now
    getOrAddSession(sessionId, expiresAt).onlineSession(expiresAt, wsSession, now)
  }
  
  fun offlineSession(sessionId: SessionId, now: Instant? = null) {
    sessions[sessionId]?.offlineSession(now)
  }
  
  fun offlineWs(sessionId: SessionId, wsSession: WsSession, now: Instant? = null) {
    sessions[sessionId]?.offlineWs(wsSession, now)
  }
  
  
  fun removeIfEmpty() {
    user.computeIfPresent(id) { _, it -> if (it.empty) null else it }
  }
}



data class SessionStatus(
  val id: SessionId,
  @Volatile var expiresAt: Instant? = null,
  @Volatile var onlineAt: Instant? = null,
  @Volatile var online: Boolean = false,
) {
  val wsSessions: MutableSet<WsSession> = concurrentSetOf()
  
  fun addWs(wsSession: WsSession) {
    wsSessions += wsSession.apply {
      sessionToWsSessions.getOrPut(id) { concurrentSetOf() } += this
      wsSessionToSessions.getOrPut(this) { concurrentSetOf() } += id
    }
  }
  
  
  val empty get() = !updateStatus().online && wsSessions.isEmpty()
  
  fun updateStatus(now: Instant? = now()) = this.also {
    online = online && !(expiresAt?.isExpired() ?: true) && wsSessions.isNotEmpty()
    if (online) onlineAt = maxOf(now, onlineAt)
    Serv.getUserBySession(id)?.updateStatus()
  }
  
  
  
  fun onlineSession(
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant? = now(),
  ) {
    this.expiresAt = expiresAt
    onlineAt = now
    online = true
    addWs(wsSession)
    updateStatus(now)
  }
  
  fun offlineSession(now: Instant? = null) {
    updateStatus(now)
    online = false
    updateStatus(now)
    removeIfEmpty()
  }
  
  fun offlineWs(wsSession: WsSession, now: Instant? = null) {
    updateStatus(now)
    wsSessions -= wsSession
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