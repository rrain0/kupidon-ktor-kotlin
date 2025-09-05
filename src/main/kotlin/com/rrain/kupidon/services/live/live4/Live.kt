package com.rrain.kupidon.services.live.live4

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Instant
import java.util.UUID




typealias UserId = UUID
typealias SessionId = UUID
typealias WsSession = DefaultWebSocketServerSession




data class SessionData(
  val id: SessionId,
  val expiresAt: Instant,
  val userId: UserId? = null,
  val onlineAt: Instant? = null,
  val online: Boolean = false,
)



enum class SessionEvType {
  OnlineUpdate,
  OnlineRemove,
}

data class SessionEv(
  val type: SessionEvType,
  val data: SessionData,
)


// Flow to push updates
val sessionOnlineUpdateFlow = MutableSharedFlow<SessionEv>()
val sessionOnlineUpdateEvents = sessionOnlineUpdateFlow.asSharedFlow()




var sessionData: SessionData? = null




suspend fun addOrUpdateSessionData(upd: SessionData) {
  val next = upd
  val curr = sessionData?.takeIf { it.id == next.id } ?: SessionData(
    id = next.id,
    expiresAt = next.expiresAt,
    userId = next.userId,
    onlineAt = null,
    online = false,
  )
  sessionData = next
  
  emitSessionUpdate(curr, next)
}

suspend fun removeSessionData(id: SessionId) {
  val curr = sessionData?.takeIf { it.id == id }
  curr ?: return
  val next = SessionData(
    id = curr.id,
    expiresAt = curr.expiresAt,
    userId = curr.userId,
    onlineAt = null,
    online = false,
  )
  sessionData = null
  
  emitSessionUpdate(curr, next)
}


suspend fun emitSessionUpdate(curr: SessionData, next: SessionData) {
  var onlineChange = false
  
  if (curr.online != next.online) {
    onlineChange = true
  }
  else if (!next.online && curr.onlineAt != next.onlineAt) {
    onlineChange = true
  }
  
  if (onlineChange) {
    sessionOnlineUpdateFlow.emit(SessionEv(SessionEvType.OnlineUpdate, next))
  }
}