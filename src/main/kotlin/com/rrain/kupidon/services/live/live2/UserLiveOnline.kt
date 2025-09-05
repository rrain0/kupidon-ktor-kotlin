package com.rrain.kupidon.services.live.live2

import com.rrain.utils.base.comparison.maxOf
import com.rrain.utils.base.collections.concurrentMapOf
import com.rrain.utils.base.collections.concurrentSetOf
import com.rrain.utils.base.`date-time`.now
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID




typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession




// Добавление сущностей идёт в порядке сверху вниз:
// user -> session -> sessionToWsSession
// wsSession -> sessionToWsSession

// Изменение / удаление сущностей идёт в порядке снизу вверх: wsSession -> session -> user.
// При добавлении сущности сначала по порядку досоздаются все верхние сущности.
// При изменении сущности изменения должны сразу распространяться вверх и проверяться, нужно ли её удалить.
// При удалении сущности верхние сущности должны сразу проверяться на то, могут ли быть удалены и удаляться.
/*
  User:
    Может иметь несколько сессий (разные браузерные контексты (обычное окно, приватное окно)).
  Session:
    Принадлежит только одному юзеру.
    Может иметь несколько вебсокет соединений (2 вкладки с ws в каждой в одном браузерном контексте).
  Web socket соединение / сессия:
    Через него могут общаться несколько сессий (мультиаккаунтность).
 */
/*
  user     1<--M  session
  guest    1<--1  session
  session  M<--M  wsSession
 */


private val userIdToUserStatus: MutableMap<UserId, UserStatus> = concurrentMapOf()
private val userIdToSessions: MutableMap<UserId, MutableSet<SessionId>> = concurrentMapOf()

private val sessionIdToSessionStatus: MutableMap<SessionId, SessionStatus> = concurrentMapOf()
private val sessionIdToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()

private val wsSessionToWsStatus: MutableMap<WsSession, WsStatus> = concurrentMapOf()
private val wsSessionToSessions: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()

/*
private val userStatus: MutableMap<UserId, UserStatus> = concurrentMapOf()
private val onlineUsers: MutableSet<UserId> = concurrentSetOf()
private val sessionToUser: MutableMap<SessionId, UserId> = concurrentMapOf()
private val sessionToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()
private val wsSessionToSessions: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()



val userOnlineChanges: MutableMap<UserId, UserOnline> = concurrentMapOf()
val sessionOnlineChanges: MutableMap<SessionId, SessionOnline> = concurrentMapOf()
*/


private fun getUser(id: UserId) = userIdToUserStatus[id]
private fun isUserOnline(id: UserId) = getUser(id)?.status?.online ?: false

private fun getOrAddUser(id: UserId) = userIdToUserStatus
  .getOrPut(id) { UserStatus(id) }

private fun getUserBySessionId(id: SessionId) = sessionIdToSessionStatus[id]
  ?.userId
  ?.let { userIdToUserStatus[it] }


private fun getOrAddSession(userId: UserId, id: SessionId) = sessionIdToSessionStatus
  .getOrPut(id) { SessionStatus(userId, id) }
  .also {
    userIdToSessions
      .getOrPut(userId) { concurrentSetOf() }
      .add(id)
  }

private fun getSessionsByUserId(id: UserId) = userIdToSessions[id]
  ?.mapNotNull { sessionIdToSessionStatus[it] }
  ?: listOf()

private fun getSessionsByWsSession(wsSession: WsSession) = wsSessionToSessions[wsSession]
  ?.mapNotNull { sessionIdToSessionStatus[it] }
  ?: listOf()


private fun getOrAddWsSession(
  sessionId: SessionId,
  wsSession: WsSession,
) = wsSessionToWsStatus
  .getOrPut(wsSession) { WsStatus(wsSession) }
  .also {
    sessionIdToWsSessions
      .getOrPut(sessionId) { concurrentSetOf() }
      .add(wsSession)
  }

private fun getWsSessionsBySessionId(id: SessionId) = sessionIdToWsSessions[id]
  ?.mapNotNull { wsSessionToWsStatus[it] }
  ?: listOf()



object UserLiveOnline {
  
  @Synchronized fun <T> use(block: (service: UserLiveOnline) -> T) = (
    block(UserLiveOnline)
  )
  
  fun onlineSession(
    userId: UserId,
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant = now(),
  ) {
    getOrAddUser(userId)
    getOrAddSession(userId, sessionId).apply {
      status = status.copy(expiresAt = expiresAt)
    }
    getOrAddWsSession(sessionId, wsSession).apply {
      update(WsUpdate(true, true, now))
    }
  }
  
  /* fun onlineUserSession(
    userId: UserId,
    sessionId: SessionId,
    expiresAt: Instant,
    wsSession: WsSession,
    now: Instant = now(),
  ) {
    getOrAddUserById(userId)
      .getOrAddSession(sessionId, expiresAt, wsSession)
      .onlineSession(expiresAt, wsSession, now)
  }
  
  fun offlineUserSession(
    userId: UserId,
    sessionId: SessionId,
    now: Instant = now(),
  ) {
    userStatus[userId]?.sessions[sessionId]?.offlineSession(now)
  }
  
  fun offlineWs(
    wsSession: WsSession,
    now: Instant = now(),
  ): List<UserStatus> {
    val users: MutableList<UserStatus> = mutableListOf()
    wsSessionToSessions.remove(wsSession)?.forEach { sessionId ->
      Srv.getUserBySession(sessionId)?.apply {
        sessions[sessionId]?.apply {
          wsSessions -= wsSession
          updateOnline(now)
          removeIfEmpty()
        }
        users += this
      }
      sessionToWsSessions.computeIfPresent(sessionId) { _, wsSessions ->
        wsSessions.apply { remove(wsSession) }.ifEmpty { null }
      }
    }
    return users
  } */
}
private val Srv = UserLiveOnline






data class UserCurr(
  val online: Boolean = false,
  val onlineAt: Instant? = null,
)
data class UserUpdate(
  val online: Boolean? = null,
  val onlineAt: Instant? = null,
)
data class UserCurrUpdate(
  var online: Boolean = false,
  var onlineAt: Instant? = null,
)


class UserStatus(
  val id: UserId,
  @Volatile var status: UserCurr = UserCurr(),
) {
  
  fun update() {
    val curr = status
    val upd = UserCurrUpdate(false, curr.onlineAt)
    getSessionsByUserId(id).forEach { it -> it.status.let { part ->
      upd.online = upd.online || part.online
      upd.onlineAt = maxOf(upd.onlineAt, part.onlineAt)
    } }
    // TODO check expiration
    //if (curr.expiresAt?.isExpired() ?: false) upd.online = false
    
    val next = UserCurr(upd.online, upd.onlineAt)
    status = next
    
    var changed = false
    if (next.online != curr.online) changed = true
    if (next.online == false && next.onlineAt != curr.onlineAt) changed = true
    
    if (changed) {
      // TODO update user
      // TODO push updates somewhere
      removeIfUnused()
    }
  }
  
  val unused get() = getSessionsByUserId(id).isEmpty()
  
  fun removeIfUnused() {
    if (unused) {
      // TODO remove from maps and from in-session map
    }
  }
}



data class SessionCurr(
  val expiresAt: Instant? = null,
  val online: Boolean = false,
  val onlineAt: Instant? = null,
)
data class SessionUpdate(
  val expiresAt: Instant? = null,
  val online: Boolean? = null,
  val onlineAt: Instant? = null,
)
data class SessionCurrUpdate(
  var expiresAt: Instant? = null,
  var online: Boolean = false,
  var onlineAt: Instant? = null,
)


class SessionStatus(
  val userId: UserId,
  val id: SessionId,
  @Volatile var status: SessionCurr = SessionCurr(),
) {
  
  fun update() {
    val curr = status
    val upd = SessionCurrUpdate(curr.expiresAt, false, curr.onlineAt)
    getWsSessionsBySessionId(id).forEach { it -> it.status.let { part ->
      upd.online = upd.online || part.online
      upd.onlineAt = maxOf(upd.onlineAt, part.onlineAt)
    } }
    // TODO check expiration
    //if (curr.expiresAt?.isExpired() ?: false) upd.online = false
    
    val next = SessionCurr(upd.expiresAt, upd.online, upd.onlineAt)
    status = next
    
    var changed = false
    if (next.online != curr.online) changed = true
    if (next.online == false && next.onlineAt != curr.onlineAt) changed = true
    
    if (changed) {
      // TODO update user
      // TODO push updates somewhere
      removeIfUnused()
    }
  }
  
  val unused get() = getWsSessionsBySessionId(id).isEmpty()
  
  fun removeIfUnused() {
    if (unused) {
      // TODO remove from maps and from in-session map
    }
  }
}



data class WsCurr(
  val active: Boolean = false,
  val online: Boolean = false,
  val onlineAt: Instant? = null,
)
data class WsUpdate(
  val active: Boolean? = null,
  val online: Boolean? = null,
  val onlineAt: Instant? = null,
)
data class WsCurrUpdate(
  var active: Boolean = false,
  var online: Boolean = false,
  var onlineAt: Instant? = null,
)


class WsStatus(
  val wsSession: WsSession,
  @Volatile var status: WsCurr = WsCurr(),
) {
  
  fun update(update: WsUpdate) {
    val curr = status
    val upd = WsCurrUpdate(
      update.active ?: curr.active,
      update.online ?: curr.online,
      update.onlineAt ?: curr.onlineAt,
    )
    val next = WsCurr(upd.active, upd.online, upd.onlineAt)
    status = next
    
    var changed = false
    if (next.active != curr.active) changed = true
    if (next.online != curr.online) changed = true
    if (next.online == false && next.onlineAt != curr.onlineAt) changed = true
    
    if (changed) {
      getSessionsByWsSession(wsSession).forEach { it.update() }
      // TODO push updates somewhere
      removeIfUnused()
    }
  }
  
  val unused get() = !status.active
  
  fun removeIfUnused() {
    if (unused) {
      // TODO remove from maps and from in-session map
    }
  }
}





