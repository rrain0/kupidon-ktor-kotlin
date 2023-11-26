package com.rrain.kupidon._old.v03.postgres.entity

import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.entity.app.Role
import java.time.LocalDate
import java.time.ZonedDateTime


data class User(
  // string UUID
  // e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val id: String? = null,
  
  // login can be email, phone, nickname#subnickname
  val email: String? = null,
  val emailVerified: Boolean? = null,
  
  // hashed password
  val pwd: String? = null,
  
  val roles: Set<Role> = setOf(),
  
  // e.g. string representation "2023-06-04 15:21:18.094+08"
  // default timezone is UTC+0
  val created: ZonedDateTime? = null,
  val updated: ZonedDateTime? = null,
  val enabled: Boolean? = null,
  
  val name: String? = null, // имя
  // e.g. string representation "2023-06-29"
  val birthDate: LocalDate? = null,
  val gender: Gender? = null, // пол: MALE / FEMALE
  
  val aboutMe: String? = null,
){
  
  fun toMapToSend(): MutableMap<String,Any?> {
    return mutableMapOf(
      "id" to id,
      "email" to email,
      "emailVerified" to emailVerified,
      "roles" to roles,
      "created" to created,
      "updated" to updated,
      "name" to name,
      "birthDate" to birthDate,
      "gender" to gender,
      "aboutMe" to aboutMe,
    )
  }
}


