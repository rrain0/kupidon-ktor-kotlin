package com.rrain.kupidon.service.sessions

import com.rrain.util.`date-time`.isExpired
import com.rrain.util.`date-time`.now
import kotlinx.datetime.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.Volatile



data object SessionsService {
  val sessionToUser: MutableMap<UUID, UUID> = ConcurrentHashMap()
  val userToSessions: MutableMap<UUID, UserSessions> = ConcurrentHashMap()
  
  fun sessions(userId: UUID) = (
    userToSessions.getOrPut(userId) { UserSessions(userId) }
  )
  fun removeIf(userId: UUID) {
    userToSessions.computeIfPresent(userId) { _, it ->
      if (!it.online && it.subscribedSessions.isEmpty()) {
        it.sessions.keys.forEach { sessionToUser.remove(it) }
        null
      }
      else it
    }
  }
}



data class UserSessions(
  val id: UUID, // this user id
) {
  constructor(id: UUID, lastStartOnlineAt: Instant) : this(id) {
    this.lastStartOnlineAt = lastStartOnlineAt
  }
  
  @Volatile var lastStartOnlineAt: Instant? = null
  val sessions: MutableMap<UUID, UserSession> = ConcurrentHashMap() // this user sessions
  val subscribedSessions: MutableSet<UUID> = ConcurrentHashMap.newKeySet() // other user ids
  
  val online get() = sessions.values.any { it.online }
  fun sessions(sessionId: UUID, expiresAt: Instant = now()) = (
    sessions.getOrPut(sessionId) { UserSession(sessionId, expiresAt) }.also {
      SessionsService.sessionToUser.put(sessionId, id)
    }
  )
}



data class UserSession(
  val id: UUID, // this session id
) {
  constructor(id: UUID, expiresAt: Instant, lastStartOnlineAt: Instant = now()) : this(id) {
    this.expiresAt = expiresAt
    this.lastStartOnlineAt = lastStartOnlineAt
  }
  
  @Volatile var lastStartOnlineAt: Instant = now()
  @Volatile var expiresAt: Instant = now()
  @Volatile var lastIsOnline: Boolean = false
  
  val online get() = lastIsOnline && !expiresAt.isExpired()
}
