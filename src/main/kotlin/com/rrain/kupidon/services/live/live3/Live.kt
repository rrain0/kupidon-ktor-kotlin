package com.rrain.kupidon.services.live.live3

import com.rrain.utils.base.collections.concurrentMapOf
import com.rrain.utils.base.collections.concurrentSetOf
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Instant
import java.util.UUID





// ПОЛЬЗОВАТЕЛЬ.
// Пользователь может иметь несколько сессий (разные устройства, приватное окно).

// СЕССИЯ.
// Сессия принадлежит одному пользователю, либо никому, если сессия гостевая.
// Сессия может иметь несколько вебсокетов (несколько вкладок).

// ВЕБСОКЕТ.
// Вебсокет может не быть ассоциирован ни с одной сессией (пользователь-гость, но в будущем будут гостевые сессии).
// Вебсокет может быть ассоциирован с несколькими сессиями (мультиаккаунтность).
//
// Клиент каждые 15 секунд должен отправлять статус по вебсокету,
// в котором будет инфа о сессиях, которые на нём сидят,
// а также статус этих сессий (онлайн/оффлайн, пользователь печатает, ...).
// Сервер каждые 20 сек чекает последнее время обновления статуса,
// если статус сессии был обновлён больше чем 20 сек назад,
// то сервер инвалидирует связь вебсокета с сессией.
//
// При открытии вебсокета, он добавляется в коллекцию вебсокетов, потом если надо, ассоциируется с сессиями.
// При закрытии вебсокета он удаляется и связанное с ним 1-к-1 (SessionToWsSession) инвалидируется.


// User  1<----M  Session  M<--(SessionToWsSession)--M  WsSession.


// Добавление сущностей идёт в порядке сверху вниз:
// user -> session -> sessionToWsSession
// wsSession -> sessionToWsSession
// При добавлении по порядку досоздаются все верхние сущности.

// Изменение сущностей идёт в порядке снизу вверх:
// user <- session <- sessionToWsSession <- wsSession
// После изменения сущности,
// верхние сущности по порядку должны провериться на изменения и возможно удалиться.

// Удаление сущностей идёт в порядке снизу вверх:
// user <- session <- sessionToWsSession
// wsSession <- sessionToWsSession





typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession




data class UserData(
  val id: UserId,
  val onlineAt: Instant? = null,
  val online: Boolean = false,
)

data class SessionData(
  val id: SessionId,
  val expiresAt: Instant,
  val userId: UserId? = null,
  val onlineAt: Instant? = null,
  val online: Boolean = false,
)

data class WsData(
  val wsSession: WsSession
)

data class SessionToWsSessionData(
  val sessionId: SessionId,
  val wsSession: WsSession,
  val updatedAt: Instant,
  val onlineAt: Instant? = null,
  val online: Boolean = false,
)



data class UserUpdate(
  var id: UserId,
  var onlineAt: Instant? = null,
  var online: Boolean? = null,
) {
  fun update(curr: UserData) = curr
    .copy(id = id)
    .let { curr -> onlineAt?.let { curr.copy(onlineAt = it) } ?: curr }
    .let { curr -> online?.let { curr.copy(online = it) } ?: curr }
}

data class SessionUpdate(
  var id: SessionId,
  var expiresAt: Instant? = null,
  var userId: UserId? = null,
  var onlineAt: Instant? = null,
  var online: Boolean? = null,
) {
  fun update(curr: SessionData) = curr
    .copy(id = id)
    .let { curr -> expiresAt?.let { curr.copy(expiresAt = it) } ?: curr }
    .let { curr -> userId?.let { curr.copy(userId = it) } ?: curr }
    .let { curr -> onlineAt?.let { curr.copy(onlineAt = it) } ?: curr }
    .let { curr -> online?.let { curr.copy(online = it) } ?: curr }
}

data class WsUpdate(
  var wsSession: WsSession,
) {
  fun update(curr: WsData) = curr
    .copy(wsSession = wsSession)
}

data class SessionToWsSessionUpdate(
  var sessionId: SessionId,
  var wsSession: WsSession,
  var updatedAt: Instant? = null,
  var onlineAt: Instant? = null,
  var online: Boolean? = null,
) {
  fun update(curr: SessionToWsSessionData) = curr
    .copy(sessionId = sessionId)
    .copy(wsSession = wsSession)
    .let { curr -> updatedAt?.let { curr.copy(updatedAt = it) } ?: curr }
    .let { curr -> onlineAt?.let { curr.copy(onlineAt = it) } ?: curr }
    .let { curr -> online?.let { curr.copy(online = it) } ?: curr }
}



data class FullSessionUpdate(
  val wsSession: WsSession,
  val sessionId: SessionId,
  val userId: UserId?,
  val expiresAt: Instant,
  val updatedAt: Instant,
  val onlineAt: Instant?,
  val online: Boolean,
)



// Main data
private val users: MutableMap<UserId, UserData> = concurrentMapOf()
private val sessions: MutableMap<SessionId, SessionData> = concurrentMapOf()
private val sessionToWsSessions: MutableMap<SessionId, MutableMap<WsSession, SessionToWsSessionData>> = concurrentMapOf()
private val wsSessions: MutableMap<WsSession, WsData> = concurrentMapOf()


// Fast search
private val userIdToSessionIds: MutableMap<UserId, MutableSet<SessionId>> = concurrentMapOf()
private val wsSessionToSessionIds: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()



data class SessionToWsSessionId(val sessionId: SessionId, val wsSession: WsSession)



data class WsSessionEv(
  val wsSession: WsSession,
  val sessionId: SessionId,
)

// Flow to push updates
val wsSessionFlow = MutableSharedFlow<WsSessionEv>()
val wsSessionEvents = wsSessionFlow.asSharedFlow()





object LiveInternal {
  
  
  
  @Synchronized
  fun <T> use(block: (service: LiveInternal) -> T): T {
    val result = block(LiveInternal)
    
    propagateChanges()
    pushUpdates()
    return result
  }
  
  
  private val updatedUsersOnline: MutableSet<UserId> = concurrentSetOf()
  
  fun pushUpdates() {
    // TODO push somewhere outside...
    updatedUsersOnline.clear()
  }
  
  
  private val changedUsers: MutableSet<UserId> = concurrentSetOf()
  private val changedSessions: MutableSet<SessionId> = concurrentSetOf()
  private val changedSessionToWsSessions: MutableSet<SessionToWsSessionId> = concurrentSetOf()
  private val changedWsSessions: MutableSet<WsSession> = concurrentSetOf()
  
  val hasChanges get() = (
    changedUsers.isNotEmpty()
  )
  
  fun propagateChanges() {
    while (hasChanges) {
      // TODO check session expiration, check if removable, propagate changes...
      TODO("Not implemented")
    }
  }
  
  
  fun addOrUpdateUser(userData: UserData) {
    userData.also { next ->
      users.compute(next.id) { k, curr ->
        
        var changed = false
        // TODO calculate changes...
        if (changed) next.id.let {
          changedUsers += it
        }
        
        next
      }
    }
  }
  
  fun addOrUpdateSession(sessionData: SessionData) {
    sessionData.also { next ->
      sessions.compute(next.id) { k, curr ->
        
        var changed = false
        // TODO calculate changes...
        if (changed) next.id.let {
          changedSessions += it
        }
        
        next
      }
    }
  }
  
  fun addOrUpdateSessionToWsSession(sessionToWsSessionData: SessionToWsSessionData) {
    sessionToWsSessionData.also { next ->
      sessionToWsSessions
        .getOrPut(next.sessionId) { mutableMapOf() }
        .compute(next.wsSession) { k, curr ->
          
          var changed = false
          // TODO calculate changes...
          if (changed) SessionToWsSessionId(next.sessionId, next.wsSession).let {
            changedSessionToWsSessions += it
          }
          
          sessionToWsSessionData
        }
      wsSessionToSessionIds
        .getOrPut(next.wsSession) { mutableSetOf() }
        .add(next.sessionId)
    }
  }
  
  fun addOrUpdateWsSession(wsData: WsData) {
    wsData.also { next ->
      wsSessions.compute(next.wsSession) { k, curr ->
        
        var changed = false
        // TODO calculate changes...
        if (changed) next.wsSession.let {
          changedWsSessions += it
        }
        
        next
      }
    }
  }
  
  
  
  
  
  
  fun removeWsSession(wsSession: WsSession) {
    val wsData = wsSessions.remove(wsSession)
    changedWsSessions += wsSession
  }
  
  
  
  
  fun onWsSessionUpdate(wsSession: WsSession) {
    val wsData = wsSessions[wsSession]
    // on removed
    if (wsData == null) {
      val sessionIds = wsSessionToSessionIds.remove(wsSession)
      sessionIds?.forEach { sessionId ->
        sessionToWsSessions.computeIfPresent(sessionId) { k, wsMap ->
          val sToWs = wsMap.remove(wsSession)
          sToWs?.also { sToWs ->
            // TODO sToWs was removed so push update to session & user
          }
          wsMap.ifEmpty { null }
        }
      }
    }
  }
  
}




object Live {
  
  fun addOrUpdateFullSession(fullSession: FullSessionUpdate) = LiveInternal.use { serv ->
    serv.addOrUpdateWsSession(WsData(
      fullSession.wsSession
    ))
    serv.addOrUpdateSession(SessionData(
      fullSession.sessionId,
      fullSession.expiresAt,
      fullSession.userId,
      fullSession.onlineAt,
      fullSession.online,
    ))
    serv.addOrUpdateSessionToWsSession(SessionToWsSessionData(
      fullSession.sessionId,
      fullSession.wsSession,
      fullSession.updatedAt,
      fullSession.onlineAt,
      fullSession.online,
    ))
    if (fullSession.userId != null) {
      serv.addOrUpdateUser(UserData(
        fullSession.userId,
        fullSession.onlineAt,
        fullSession.online,
      ))
    }
  }
  
  fun removeWsSession(wsSession: WsSession) = LiveInternal.use { serv -> serv.removeWsSession(wsSession) }
  
}

