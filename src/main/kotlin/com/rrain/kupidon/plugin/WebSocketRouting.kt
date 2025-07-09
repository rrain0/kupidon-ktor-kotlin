package com.rrain.kupidon.plugin

import com.fasterxml.jackson.module.kotlin.readValue
import com.rrain.kupidon.service.AccessToken
import com.rrain.kupidon.service.sessions.UserLiveStatusService
import com.rrain.util.any.cast
import com.rrain.util.uuid.toUuid
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import java.util.UUID
import kotlin.time.Duration.Companion.seconds



data class WsMsg(val type: String, val data: Map<String, Any?> = mapOf())

fun WsMsgToClient(type: String, data: Map<String, Any?> = mapOf()) = (
  mapOf("type" to "TO_CLIENT", "data" to WsMsg(type, data))
)


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
              
              when (ev.type) {
                
                "BECAME_ONLINE" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  println("BECAME_ONLINE userId: $userId, sessionId: $sessionId")
                  
                  UserLiveStatusService.onlineUserSession(userId, sessionId, sessionExpiresAt, this).apply {
                    watchers
                    .flatMap { UserLiveStatusService.sessionToWsSessions[it] ?: emptySet() }
                    .forEach {
                      it.sendSerialized(WsMsgToClient(
                        "USERS_STATUS_UPDATE",
                        mapOf("usersStatus" to listOf(UserStatus(userId, online = true)))
                      ).also { println("WS send: $it") })
                    }
                  }
                }
                
                "BECAME_OFFLINE" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  println("BECAME_OFFLINE userId: $userId, sessionId: $sessionId")
                  
                  UserLiveStatusService.offlineUserSession(userId, sessionId)?.apply {
                    watchers
                    .flatMap { UserLiveStatusService.sessionToWsSessions[it] ?: emptySet() }
                    .forEach {
                      it.sendSerialized(WsMsgToClient(
                        "USERS_STATUS_UPDATE",
                        mapOf("usersStatus" to listOf(UserStatus(userId, online = online())))
                      ).also { println("WS send: $it") })
                    }
                  }
                }
                
                "SUBSCRIBE_ON_USERS_STATUS" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  val watchUserIds = ev.data["userIds"].cast<List<String>>().map { it.toUuid() }.toSet()
                  
                  println("SUBSCRIBE_ON_USERS_STATUS userId: " +
                    "$userId, sessionId: $sessionId, userIds: $watchUserIds"
                  )
                  
                  UserLiveStatusService
                    .userOrCreate(userId)
                    .sessionOrCreate(sessionId, sessionExpiresAt)
                    .addWs(this)
                  
                  UserLiveStatusService.replaceWatched(sessionId, watchUserIds)
                  
                  val watchedUsersStatus = watchUserIds
                    .mapNotNull { UserLiveStatusService.user[it] }
                    .map { UserStatus(it.id, online = it.online()) }
                  
                  UserLiveStatusService.sessionToWsSessions[sessionId]?.forEach {
                    it.sendSerialized(WsMsgToClient(
                      "USERS_STATUS_UPDATE",
                      mapOf("usersStatus" to watchedUsersStatus)
                    ).also { println("WS send: $it") })
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
        UserLiveStatusService.offlineWs(this).forEach { it.apply {
          watchers
            .flatMap { UserLiveStatusService.sessionToWsSessions[it] ?: emptySet() }
            .forEach {
              it.sendSerialized(WsMsgToClient(
                "USERS_STATUS_UPDATE",
                mapOf("usersStatus" to listOf(UserStatus(id, online = online())))
              ).also { println("WS send: $it") })
            }
        } }
      }
    }
  }
  
}
