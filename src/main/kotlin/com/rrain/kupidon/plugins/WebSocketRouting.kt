package com.rrain.kupidon.plugins

import com.fasterxml.jackson.module.kotlin.readValue
import com.rrain.kupidon.services.jwt.AccessToken
import com.rrain.kupidon.services.live.live3.LiveInternal
import com.rrain.kupidon.services.live.live3.WsData
import com.rrain.kupidon.services.live.UserLiveStatusService
import com.rrain.kupidon.services.live.UserStatus
import com.rrain.kupidon.services.live.live3.FullSessionUpdate
import com.rrain.kupidon.services.live.live3.Live
import com.rrain.utils.base.any.cast
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.base.uuid.toUuid
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import java.util.UUID
import kotlin.time.Duration.Companion.seconds



data class WsMsgMap(val type: String, val data: Map<String, Any?> = mapOf())
data class WsMsg<T>(val type: String, val data: T)

fun WsMsgToClient(type: String, data: Map<String, Any?> = mapOf()) =
  WsMsg("TO_CLIENT", WsMsgMap(type, data))



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
  
  
  data class UserStatusToApi(val id: UUID, val online: Boolean)
  
  
  routing {
    webSocket("/ws") {
      // 'this' is websocket session
      
      /*
      outgoing.send(Frame.Text("YOU SAID: $text"))
    
      if (text.equals("bye", ignoreCase = true)) {
        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
      }
      */
      
      // Register opened websocket connection
      LiveInternal.use { serv ->
        serv.addOrUpdateWsSession(WsData(this))
      }
      
      try {
        for (frame in incoming) {
          if (frame is Frame.Text) {
            try {
              val textData = frame.readText()
              val ev = JacksonObjectMapper.readValue<WsMsgMap>(textData)
              val now = now()
              println("WebSocket received: $ev")
              
              when (ev.type) {
                
                "SESSIONS_STATUS" -> {
                  val sessionsData: SessionsDataIn =
                    JacksonObjectMapper.readValue<WsMsg<SessionsDataIn>>(textData).data
                  sessionsData.sessions.forEach { sessionsData ->
                    val accessToken = AccessToken(sessionsData.accessToken, noVerify = true)
                    // TODO Check session validity, permission to see other user's info
                    
                    accessToken.also { tok ->
                      val userId = tok.userId
                      val sessionId = tok.sessionId
                      val sessionExpiresAt = tok.sessionExpiresAt
                      val online = sessionsData.sessionOnline
                      Live.addOrUpdateFullSession(FullSessionUpdate(
                        wsSession = this,
                        sessionId = sessionId,
                        userId = userId,
                        expiresAt = sessionExpiresAt,
                        updatedAt = now,
                        onlineAt = if (online) now else null,
                        online = online,
                      ))
                    }
                  }
                }
                
                
                
                "BECAME_ONLINE" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  println("BECAME_ONLINE userId: $userId, sessionId: $sessionId")
                  
                  val userStatus = UserLiveStatusService.use { service ->
                    service.onlineUserSession(userId, sessionId, sessionExpiresAt, this)
                  }
                  userStatus.apply {
                    val msg = WsMsgToClient(
                      "USERS_STATUS_UPDATE",
                      mapOf("usersStatus" to listOf(UserStatusToApi(userId, online = online)))
                    ).also { println("WS send: $it") }
                    watchers
                      .flatMap { UserLiveStatusService.getWsSessionBySession(it) ?: emptySet() }
                      .forEach { it.sendSerialized(msg) }
                  }
                }
                
                "BECAME_OFFLINE" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  println("BECAME_OFFLINE userId: $userId, sessionId: $sessionId")
                  
                  val userStatus = UserLiveStatusService.use { service ->
                    service.offlineUserSession(userId, sessionId)
                  }
                  userStatus?.apply {
                    val msg = WsMsgToClient(
                      "USERS_STATUS_UPDATE",
                      mapOf("usersStatus" to listOf(UserStatusToApi(userId, online = online)))
                    ).also { println("WS send: $it") }
                    watchers
                      .flatMap { UserLiveStatusService.getWsSessionBySession(it) ?: emptySet() }
                      .forEach { it.sendSerialized(msg) }
                  }
                }
                
                "SUBSCRIBE_ON_USERS_STATUS" -> {
                  val accessToken = AccessToken(ev.data["accessToken"] as String, noVerify = true)
                  val userId = accessToken.userId
                  val sessionId = accessToken.sessionId
                  val sessionExpiresAt = accessToken.sessionExpiresAt
                  
                  val watchedUserIds = ev.data["userIds"].cast<List<String>>().map { it.toUuid() }.toSet()
                  
                  println("SUBSCRIBE_ON_USERS_STATUS userId: " +
                    "$userId, sessionId: $sessionId, userIds: $watchedUserIds"
                  )
                  
                  UserLiveStatusService.use { service ->
                    service
                      .getOrAddUser(UserStatus(userId))
                      .getOrAddSession(sessionId, sessionExpiresAt, this)
                    service.replaceWatched(sessionId, watchedUserIds)
                  }
                  
                  val watchedUsersStatus = watchedUserIds
                    .mapNotNull { UserLiveStatusService.getUser(it) }
                    .map { UserStatusToApi(it.id, online = it.online) }
                  
                  val msg = WsMsgToClient(
                    "USERS_STATUS_UPDATE",
                    mapOf("usersStatus" to watchedUsersStatus)
                  ).also { println("WS send: $it") }
                  UserLiveStatusService.getWsSessionBySession(sessionId)?.forEach {
                    it.sendSerialized(msg)
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
        Live.removeWsSession(this)
        
        UserLiveStatusService.use { service -> service.offlineWs(this) }
          .forEach { userStatus -> userStatus.apply {
            val msg = WsMsgToClient(
              "USERS_STATUS_UPDATE",
              mapOf("usersStatus" to listOf(UserStatusToApi(id, online = online)))
            ).also { println("WS send: $it") }
            watchers
              .flatMap { UserLiveStatusService.getWsSessionBySession(it) ?: emptySet() }
              .forEach { it.sendSerialized(msg) }
          } }
      }
    }
  }
  
}



data class SessionDataIn(
  val accessToken: String,
  val sessionOnline: Boolean,
)

data class SessionsDataIn(
  val sessions: List<SessionDataIn>
)


