package com.rrain.kupidon.service.db.mongo.model

import com.rrain.kupidon.model.Gender
import com.rrain.kupidon.model.Role
import com.rrain.util.`date-time`.getAge
import org.bson.Document
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID


enum class UserDataType { Full, Current, Other }


data class UserMongo(
  // string UUID
  // e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val id: UUID,
  
  val roles: Set<Role>,
  
  // login can be email, phone, nickname#subnickname
  val email: String,
  
  // hashed password
  val pwd: String,
  
  // "2023-06-04T15:21:18.094+08:00" in string
  // default timezone is UTC+0
  val created: ZonedDateTime,
  val updated: ZonedDateTime,
  
  // имя
  val name: String,
  // "2023-06-29" in string
  val birthDate: LocalDate,
  // пол: 'MALE' / 'FEMALE'
  val gender: Gender,
  val aboutMe: String,
  val photos: List<UserProfilePhotoMetadataMongo>,
  
  val transactions: Document?,
){
  
  fun convertToSend(
    userType: UserDataType = UserDataType.Other,
    host: String,
    port: Int,
  ): MutableMap<String,Any?> {
    val lvl = when (userType) {
      UserDataType.Full -> 2
      UserDataType.Current -> 1
      UserDataType.Other -> 0
    }
    val data = mutableMapOf<String,Any?>(
      "id" to id,
      "name" to name,
      "birthDate" to birthDate, // TODO replace by age
      "age" to getAge(birthDate), // TODO use time zone from client (url param or request header)
      "gender" to gender,
      "aboutMe" to aboutMe,
      "photos" to photos.map { it.convertToSend(id, host, port) },
    )
    
    if (lvl >= 1) data.putAll(listOf(
      "roles" to roles,
      "email" to email,
      // todo email
      "emailVerified" to true,
      "transactions" to transactions,
    ))
    
    if (lvl >= 2) data.putAll(listOf(
      "pwd" to pwd, // hashed pwd
      "created" to created,
      "updated" to updated,
    ))
    
    return data
  }
  
}
