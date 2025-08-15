package com.rrain.kupidon.service.live

import com.rrain.util.base.collections.concurrentMapOf
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Instant
import java.util.UUID




// Вебсокет.
// Вебсокет может не быть ассоциирован ни с одной сессией (пользователь-гость).
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
// При закрытии вебсокета он удаляется и связанное с ним 1-к-1 инвалидируется.

// Сессия.
// Сессия принадлежит одному пользователю, либо никому, если сессия гостевая.
// Сессия может иметь несколько вебсокетов (несколько вкладок).

// Пользователь.
// Пользователь может иметь нескольо сессий (разные устройства, приватное окно).



typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession




data class WsData(
  val wsSession: WsSession
)

data class SessionData(
  val id: SessionId,
  val userId: UserId?,
  val expiresAt: Instant,
  val onlineAt: Instant?,
  val online: Boolean,
)

data class UserData(
  val id: UserId,
  val onlineAt: Instant?,
  val online: Boolean,
)

data class SessionToWsSessionData(
  val sessionId: SessionId,
  val wsSession: WsSession,
  val updatedAt: Instant,
  val onlineAt: Instant?,
  val online: Boolean,
)

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
private val wsSessions: MutableMap<WsSession, WsData> = concurrentMapOf()
private val sessions: MutableMap<SessionId, SessionData> = concurrentMapOf()
private val users: MutableMap<UserId, UserData> = concurrentMapOf()
private val sessionToWsSessions: MutableMap<SessionId, MutableMap<WsSession, SessionToWsSessionData>> = concurrentMapOf()


// Fast search
private val userIdToSessionIds: MutableMap<UserId, MutableSet<SessionId>> = concurrentMapOf()
private val wsSessionToSessionIds: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()





data class WsSessionEv(
  val wsSession: WsSession,
  val sessionId: SessionId,
)

val wsSessionFlow = MutableSharedFlow<WsSessionEv>()
val wsSessionEvents = wsSessionFlow.asSharedFlow()





object Live {
  
  
  
  @Synchronized
  fun <T> use(block: (service: Live) -> T): T {
    val result = block(Live)
    
    return result
  }
  
  @Synchronized
  fun addOrUpdateWsSession(wsData: WsData) {
    wsData.also { next ->
      wsSessions.compute(next.wsSession) { k, curr ->
        // Here you can calculate changes and notify about state updates
        next
      }
    }
  }
  
  @Synchronized
  fun removeWsSession(wsSession: WsSession) {
    val wsData = wsSessions.remove(wsSession)
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
  
  @Synchronized
  fun addOrUpdateSession(sessionData: SessionData) {
    sessionData.also { next ->
      sessions.compute(next.id) { k, curr ->
        // TODO calculate changes and notify
        next
      }
    }
  }
  
  @Synchronized
  fun addOrUpdateUser(userData: UserData) {
    userData.also { next ->
      users.compute(next.id) { k, curr ->
        // TODO calculate changes and notify
        next
      }
    }
  }
  
  @Synchronized
  fun addOrUpdateSessionToWsSession(sessionToWsSessionData: SessionToWsSessionData) {
    sessionToWsSessionData.also { next ->
      sessionToWsSessions
        .getOrPut(next.sessionId) { mutableMapOf() }
        .compute(next.wsSession) { k, curr ->
          // TODO calculate changes and notify
          sessionToWsSessionData
        }
      wsSessionToSessionIds
        .getOrPut(next.wsSession) { mutableSetOf() }
        .add(next.sessionId)
    }
  }
  
  @Synchronized
  fun addOrUpdateFullSession(fullSession: FullSessionUpdate) {
    addOrUpdateWsSession(WsData(
      fullSession.wsSession
    ))
    addOrUpdateSession(SessionData(
      fullSession.sessionId,
      fullSession.userId,
      fullSession.expiresAt,
      fullSession.onlineAt,
      fullSession.online,
    ))
    addOrUpdateSessionToWsSession(SessionToWsSessionData(
      fullSession.sessionId,
      fullSession.wsSession,
      fullSession.updatedAt,
      fullSession.onlineAt,
      fullSession.online,
    ))
    if (fullSession.userId != null) {
      addOrUpdateUser(UserData(
        fullSession.userId,
        fullSession.onlineAt,
        fullSession.online,
      ))
    }
  }
  
}

