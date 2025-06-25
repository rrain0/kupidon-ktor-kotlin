package com.rrain.kupidon.service.mongo.model

import com.mongodb.kotlin.client.coroutine.FindFlow
import com.rrain.kupidon.model.Gender
import com.rrain.kupidon.model.Role
import com.rrain.util.`date-time`.getAge
import kotlinx.datetime.Instant
import org.bson.Document
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.util.UUID


enum class UserDataType { Full, Current, Other }


data class UserM(
  // string UUID
  // e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  var id: UUID,
  
  var roles: Set<Role>,
  
  // login can be email, phone, nickname#subnickname
  var email: String,
  
  // hashed password
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
    userType: UserDataType = UserDataType.Other,
    host: String,
    port: Int,
    // TODO pass timeZone query param for each user or try get from user agent string
    timeZone: TimeZone = TimeZone.UTC,
  ): MutableMap<String, Any?> {
    val lvl = when (userType) {
      UserDataType.Full -> 2
      UserDataType.Current -> 1
      UserDataType.Other -> 0
    }
    val data = mutableMapOf<String, Any?>(
      "id" to id,
      "name" to name,
      "birthDate" to birthDate, // TODO replace by age
      "age" to getAge(birthDate, timeZone),
      "gender" to gender,
      "aboutMe" to aboutMe,
      "photos" to photos.map { it.toApi(id, host, port) },
    )
    
    if (lvl >= 1) data.putAll(listOf(
      "roles" to roles,
      "email" to email,
      // todo email
      "emailVerified" to true,
    ))
    
    if (lvl >= 2) data.putAll(listOf(
      "pwd" to pwd, // hashed pwd
      "createdAt" to createdAt,
      "updatedAt" to updatedAt,
    ))
    
    return data
  }
  
}




val projectionUserM = Document(
  "${UserM::photos.name}.${UserProfilePhotoM::binData.name}", false
)

fun FindFlow<UserM>.projectionUserM(): FindFlow<UserM> {
  return projection(projectionUserM)
}
