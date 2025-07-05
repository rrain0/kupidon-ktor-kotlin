package com.rrain.kupidon.service.sessions

import com.rrain.util.`date-time`.isExpired
import com.rrain.util.`date-time`.now
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.datetime.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap



typealias UserId = UUID
typealias SessionId = UUID


object UserLiveStatusService {
  val userById: MutableMap<UserId, UserStatus> = ConcurrentHashMap()
  
  fun userByIdOrCreate(userId: UserId) = userById.getOrPut(userId) { UserStatus(userId) }
  
}


data class UserStatus(
  val id: UserId,
  @Volatile var lastStartOnlineAt: Instant? = null,
) {
  val sessions: MutableMap<SessionId, SessionStatus> = ConcurrentHashMap()
  val watchers: MutableSet<SessionId> = ConcurrentHashMap.newKeySet()
  
  val online get() = sessions.values.any { it.online }
    .also { online -> if (online) lastStartOnlineAt = maxOf(lastStartOnlineAt ?: now(), now()) }
  val canRemove get() = sessions.isEmpty() && watchers.isEmpty()
}


data class SessionStatus(
  val id: SessionId,
  @Volatile var expiresAt: Instant? = null,
  @Volatile var lastStartOnlineAt: Instant? = null,
  @Volatile var lastIsOnline: Boolean = false,
) {
  val wsSessions: MutableSet<DefaultWebSocketServerSession> = ConcurrentHashMap.newKeySet()
  
  val online get() = lastIsOnline && !(expiresAt?.isExpired() ?: true) && wsSessions.isNotEmpty()
    .also { online -> if (online) lastStartOnlineAt = maxOf(lastStartOnlineAt ?: now(), now()) }
  val canRemove get() = !online && wsSessions.isEmpty()
}