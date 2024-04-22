package com.rrain.kupidon.service.db.mongo.model

import com.rrain.kupidon.model.Gender
import com.rrain.kupidon.model.Role
import io.ktor.server.request.*
import org.bson.Document
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID



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
  
  fun convertToSend(request: ApplicationRequest): MutableMap<String,Any?> {
    return mutableMapOf(
      "id" to id,
      "roles" to roles,
      "email" to email,
      //"emailVerified" to true, // todo
      "created" to created,
      "updated" to updated,
      "name" to name,
      "birthDate" to birthDate,
      "gender" to gender,
      "aboutMe" to aboutMe,
      "photos" to photos.map { it.convertToSend(id, request) },
      "transactions" to transactions,
    )
  }
  
}
