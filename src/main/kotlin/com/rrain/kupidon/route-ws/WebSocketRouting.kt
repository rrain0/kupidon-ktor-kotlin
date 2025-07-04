package com.rrain.kupidon.`route-ws`

import com.fasterxml.jackson.module.kotlin.readValue
import com.rrain.kupidon.plugin.JacksonObjectMapper
import com.rrain.kupidon.service.AccessToken
import com.rrain.kupidon.service.sessions.SessionsService
import com.rrain.util.any.cast
import com.rrain.util.`date-time`.now
import com.rrain.util.uuid.toUuid
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds




object WsSessions {
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



data class WsMsg(val type: String, val data: Map<String, Any?> = mapOf())

data class WsMsgToClient (val type: String = "TO_CLIENT", val data: WsMsg) {
  constructor(type: String, data: Map<String, Any?> = mapOf())
    : this("TO_CLIENT", WsMsg(type, data))
}



fun Application.configureWebSocketRouting() {
  
  
  install(WebSockets) {
    pingPeriod = 15.seconds
    timeout = 15.seconds
    maxFrameSize = Long.MAX_VALUE
    masking = false
    contentConverter = object : WebsocketContentConverter {
      override suspend fun serialize(
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
      ): Frame {
        return JacksonObjectMapper.writeValueAsString(value).let { Frame.Text(it) }
      }
      
      override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: Frame,
      ): Any? {
        return JacksonObjectMapper.readValue(
          content.cast<Frame.Text>().readText(),
          typeInfo.type.java
        )
      }
      
      override fun isApplicable(frame: Frame): Boolean {
        return frame is Frame.Text
      }
    }
  }
  
  
  data class UserStatus(val id: UUID, val online: Boolean)
  
  
  routing {
    webSocket("/ws") {
      // 'this' is websocket session
      
      /*
      outgoing.send(Frame.Text("YOU SAID: $text"))
    
      if (text.equals("bye", ignoreCase = true)) {
        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
      }
      */
      
      try {
        for (frame in incoming) {
          if (frame is Frame.Text) {
            try {
              val ev = JacksonObjectMapper.readValue<WsMsg>(frame.readText())
              println("WebSocket received: $ev")
              val now = now()
              when (ev.type) {
                "BECAME_ONLINE" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  println("BECAME_ONLINE userId: $userId, sessionId: $sessionId")
                  
                  WsSessions.addWsSession(this, sessionId)
                  
                  SessionsService.becameOnline(userId, sessionId, sessionExpiresAt).apply {
                    subscribedSessions.forEach {
                      WsSessions.getWsSessions(it)?.forEach {
                        it.sendSerialized(WsMsgToClient(
                          "USERS_STATUS_UPDATE",
                          mapOf("usersStatus" to listOf(UserStatus(userId, online = true)))
                        ))
                      }
                    }
                  }
                }
                
                "BECAME_OFFLINE" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  println("BECAME_OFFLINE userId: $userId, sessionId: $sessionId")
                  
                  WsSessions.addWsSession(this, sessionId)
                  
                  
                  SessionsService.becameOffline(userId, sessionId, sessionExpiresAt).apply {
                    subscribedSessions.forEach {
                      WsSessions.getWsSessions(it)?.forEach {
                        it.sendSerialized(WsMsgToClient(
                          "USERS_STATUS_UPDATE",
                          mapOf("usersStatus" to listOf(UserStatus(userId, online = online)))
                        ))
                      }
                    }
                  }
                }
                
                "SUBSCRIBE_ON_USERS_STATUS" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  
                  val watchUserIds = ev.data["userIds"].cast<List<String>>().map { it.toUuid() }.toSet()
                  
                  println("SUBSCRIBE_ON_USERS_STATUS userId: " +
                    "$userId, sessionId: $sessionId, userIds: $watchUserIds"
                  )
                  
                  WsSessions.addWsSession(this, sessionId)
                  
                  val subscribedUsersStatus = mutableListOf<UserStatus>()
                  
                  // Добавляемся юзерам в подписчики + берём их статус
                  // Если юзера не было, то создаём!!!
                  watchUserIds.forEach {
                    SessionsService.user(it).apply {
                      subscribedSessions += sessionId
                      subscribedUsersStatus += UserStatus(id, online = online)
                    }
                  }
                  // Удаляемся из подписчиков тех юзеров, статус которых нам больше не нужен
                  SessionsService.userToSessions.forEach { (userId, sessions) ->
                    if (userId !in watchUserIds) sessions.subscribedSessions -= sessionId
                  }
                  // Отправляем текущий статус юзеров
                  WsSessions.getWsSessions(sessionId)?.forEach {
                    it.sendSerialized(WsMsgToClient(
                      "USERS_STATUS_UPDATE",
                      mapOf("usersStatus" to subscribedUsersStatus)
                    ))
                  }
                }
                
              }
            }
            catch (ex: Exception) {
              ex.printStackTrace()
            }
          }
        }
      }
      finally {
        SessionsService.becameOffline(this)
        WsSessions.removeWsSession(this)
      }
    }
  }
  
}
