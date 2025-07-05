package com.rrain.kupidon.service.sessions

import io.ktor.server.websocket.DefaultWebSocketServerSession
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap



// TODO remove
object WsSessionsService {
  val userSessionToWsSessions: MutableMap<UUID, MutableSet<DefaultWebSocketServerSession>> = ConcurrentHashMap()
  val wsSessionToUserSessions: MutableMap<DefaultWebSocketServerSession, MutableSet<UUID>> = ConcurrentHashMap()
  fun addWsSession(wsSession: DefaultWebSocketServerSession, userSession: UUID) {
    userSessionToWsSessions.getOrPut(userSession) { ConcurrentHashMap.newKeySet() } += wsSession
    wsSessionToUserSessions.getOrPut(wsSession) { ConcurrentHashMap.newKeySet() } += userSession
  }
  fun removeWsSession(wsSession: DefaultWebSocketServerSession) {
    wsSessionToUserSessions.remove(wsSession)?.forEach {
      userSessionToWsSessions.computeIfPresent(it) { k, v ->
        v.remove(wsSession)
        if (v.isEmpty()) null else v
      }
    }
  }
  fun getWsSessions(userSession: UUID) = userSessionToWsSessions[userSession]
}