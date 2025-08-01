package com.rrain.kupidon.service.live

import com.rrain.util.base.any.mapNull
import com.rrain.util.base.any.maxOf
import com.rrain.util.base.collections.concurrentMapOf
import com.rrain.util.base.collections.concurrentSetOf
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
/*
  User
    Может иметь несколько сессий (разные браузерные контексты (обычное окно, приватное окно)).
  Session
    Принадлежит только одному юзеру.
    Может иметь несколько вебсокет соединений (2 вкладки с ws в каждой в одном браузерном контексте).
  Web socket соединение / сессия
    Через него могут общаться несколько сессий (мультиаккаунтность).
 */


private val userIdToUserStatus: MutableMap<UserId, UserStatus> = concurrentMapOf()
private val userIdToSessions: MutableMap<UserId, MutableSet<SessionStatus>> = concurrentMapOf()

private val sessionIdToSessionStatus: MutableMap<SessionId, SessionStatus> = concurrentMapOf()
private val sessionIdToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()

private val wsSessionToWsStatus: MutableMap<WsSession, WsStatus> = concurrentMapOf()

/*
private val userStatus: MutableMap<UserId, UserStatus> = concurrentMapOf()
private val onlineUsers: MutableSet<UserId> = concurrentSetOf()
private val sessionToUser: MutableMap<SessionId, UserId> = concurrentMapOf()
private val sessionToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()
private val wsSessionToSessions: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()



val userOnlineChanges: MutableMap<UserId, UserOnline> = concurrentMapOf()
val sessionOnlineChanges: MutableMap<SessionId, SessionOnline> = concurrentMapOf()
*/



object UserLiveOnline {
  
  @Synchronized fun <T> use(block: (service: UserLiveOnline) -> T) = (
    block(UserLiveOnline)
  )
  
  /*
  fun getUser(id: UserId) = userStatus[id]
  fun isUserOnline(id: UserId) = getUser(id)?.online.mapNull { false }
  
  fun getOrAddUser(userToAdd: UserStatus) = userStatus.getOrPut(userToAdd.id) { userToAdd }
  
  fun getUserBySession(id: SessionId) = sessionToUser[id]?.let { userStatus[it] }
  fun getWsSessionBySession(id: SessionId) = sessionToWsSessions[id]
  
  
  fun getOrAddUserById(userId: UserId) = getOrAddUser(UserStatus(userId))
   */
  
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
  val sessions get() = userIdToSessions[id] ?: setOf()
  
  fun update() {
    val curr = status
    val upd = UserCurrUpdate(false, curr.onlineAt)
    sessions.forEach { it -> it.status.let { part ->
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
  
  val unused get() = sessions.isEmpty()
  
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
  val id: SessionId,
  @Volatile var status: SessionCurr = SessionCurr(),
) {
  
  val wsSessions get() = sessionIdToWsSessions[id]
    ?.mapNotNull { wsSessionToWsStatus[it] }
    ?: listOf()
  
  fun update() {
    val curr = status
    val upd = SessionCurrUpdate(curr.expiresAt, false, curr.onlineAt)
    wsSessions.forEach { it -> it.status.let { part ->
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
  
  val unused get() = wsSessions.isEmpty()
  
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
      // TODO update session
      // TODO push updates somewhere
      removeIfUnused()
    }
  }
  
  // This is just a shortcut
  fun online(onlineAt: Instant) {
    update(WsUpdate(online = true, onlineAt = onlineAt))
  }
  
  // This is just a shortcut
  fun offline(onlineAt: Instant? = null) {
    update(WsUpdate(online = false, onlineAt = onlineAt))
  }
  
  val unused get() = !status.active
  
  fun removeIfUnused() {
    if (unused) {
      // TODO remove from maps and from in-session map
    }
  }
}





