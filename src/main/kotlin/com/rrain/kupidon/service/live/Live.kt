package com.rrain.kupidon.service.live

import com.rrain.util.base.collections.concurrentMapOf
import com.rrain.util.base.collections.concurrentSetOf
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID




// Вебсокет может не быть ассоциирован ни с одной сессией (пользователь-гость).
// Вебсокет может быть ассоциирован с несколькими сессиями (мультиаккаунтность).
// При открытии вебсокета, он добавляется сюда, потом если надо, ассоциируется с сессиями.
// При закрытии вебсокета - удаляется.

// Сессия принадлежит одному пользователю, либо никому, если сессия гостевая.
// Сессия может иметь несколько вебсокетов (несколько вкладок).

// Пользователь может иметь нескольо сессий (разные устройства, приватное окно).



typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession


// Main data
val wsSessions: MutableMap<WsSession, WsData> = concurrentMapOf()
val sessions: MutableMap<SessionId, SessionData> = concurrentMapOf()
val users: MutableMap<UserId, UserData> = concurrentMapOf()

// Fast search
val userIdToSessionIds: MutableMap<UserId, MutableSet<SessionId>> = concurrentMapOf()
val sessionIdToWsSessions: MutableMap<SessionId, MutableSet<WsSession>> = concurrentMapOf()
val wsSessionToSessionIds: MutableMap<WsSession, MutableSet<SessionId>> = concurrentMapOf()




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