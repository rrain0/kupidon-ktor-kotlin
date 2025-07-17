package com.rrain.kupidon.model.db

import com.mongodb.kotlin.client.coroutine.FindFlow
import com.rrain.kupidon.model.Gender
import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.sessions.UserLiveStatusService
import com.rrain.util.base.`date-time`.getAge
import kotlinx.datetime.Instant
import org.bson.Document
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.util.UUID


enum class UserDataType {
  Full,
  Current,
  //Acquaintance,
  AcquaintanceShort,
  Stranger,
  StrangerShort
}


data class UserM(
  // string UUID
  // e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  var id: UUID,
  
  var roles: Set<Role>,
  
  // login can be email, phone, nickname#subnickname
  var email: String,
  
  // Password hash
  var pwd: String,
  
  // "2023-06-04T15:21:18.094Z" in string
  var createdAt: Instant,
  var updatedAt: Instant,
  
  // имя
  var name: String,
  // "2023-06-29" in string
  var birthDate: LocalDate,
  // пол: 'MALE' / 'FEMALE'
  var gender: Gender,
  var aboutMe: String,
  var photos: List<UserProfilePhotoMetadataM>,
) {
  
  fun toApi(
    userType: UserDataType = UserDataType.Stranger,
    host: String,
    port: Int,
    // TODO pass timeZone query param for each user or try get from user agent string
    timeZone: TimeZone = TimeZone.UTC,
    showStatus: Boolean = false,
  ): MutableMap<String, Any?> {
    return when (userType) {
      UserDataType.Full -> mutableMapOf(
        "id" to id,
        "roles" to roles,
        "email" to email,
        "pwd" to pwd, // hashed pwd
        // todo email verification
        //"emailVerified" to true,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "name" to name,
        "ava" to ava(host, port),
        "birthDate" to birthDate,
        "age" to getAge(birthDate, timeZone),
        "gender" to gender,
        "aboutMe" to aboutMe,
        "photos" to photos.map { it.toApi(id, host, port) },
        //"onlineAt" to
        "online" to UserLiveStatusService.isUserOnline(id),
      )
      UserDataType.Current -> mutableMapOf(
        "id" to id,
        "roles" to roles,
        "email" to email,
        // todo email verification
        //"emailVerified" to true,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "name" to name,
        "ava" to ava(host, port),
        "birthDate" to birthDate,
        "age" to getAge(birthDate, timeZone),
        "gender" to gender,
        "aboutMe" to aboutMe,
        "photos" to photos.map { it.toApi(id, host, port) },
        //"onlineAt" to
        "online" to UserLiveStatusService.isUserOnline(id),
      )
      UserDataType.AcquaintanceShort -> mutableMapOf(
        "id" to id,
        "name" to name,
        "ava" to ava(host, port),
        "online" to UserLiveStatusService.isUserOnline(id),
      )
      UserDataType.Stranger -> mutableMapOf(
        "id" to id,
        "name" to name,
        "ava" to ava(host, port),
        "age" to getAge(birthDate, timeZone),
        "gender" to gender,
        "aboutMe" to aboutMe,
        "photos" to photos.map { it.toApi(id, host, port) },
      )
      UserDataType.StrangerShort -> mutableMapOf(
        "id" to id,
        "name" to name,
        "ava" to ava(host, port),
      )
    }
  }
  
  fun ava(
    host: String,
    port: Int,
  ) = (
    photos.find { it.index == 0 }?.getUrl(id, host, port) ?: ""
  )
  
}




val projectionUserM = Document(
  "${UserM::photos.name}.${UserProfilePhotoM::binData.name}", false
)

fun FindFlow<UserM>.projectionUserM(): FindFlow<UserM> = (
  projection(projectionUserM)
)
