package com.rrain.kupidon.service.sessions

import com.rrain.util.`date-time`.now
import kotlinx.datetime.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.Volatile



data object SessionsService {
  val sessions: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
  val userSessions: MutableMap<UUID, UserSessions> = ConcurrentHashMap()
}



data class UserSessions(
  val id: UUID, // this user id
  @Volatile var lastStartOnlineAt: Instant? = null,
  val sessions: MutableMap<UUID, UserSession> = ConcurrentHashMap(), // this user sessions
  val onlineStatusSubscribers: MutableSet<UUID> = ConcurrentHashMap.newKeySet(), // other user ids
)

data class UserSession(
  val id: UUID, // this session id
  @Volatile var lastStartOnlineAt: Instant = now(),
  @Volatile var expiresAt: Instant,
)