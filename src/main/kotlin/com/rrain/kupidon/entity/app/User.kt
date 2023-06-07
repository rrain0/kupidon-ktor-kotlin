package com.rrain.kupidon.entity.app

import java.time.LocalDate
import java.time.ZonedDateTime


data class User(
  val id: String? = null, // string UUID
  
  // login can be email, phone, nickname#subnickname
  val email: String? = null, // email, phone etc
  val pwd: String? = null,
  
  val roles: Set<Role> = setOf(),
  
  // must be unique(nickname, subnickname)
  // nickname#subnickname (rrain#cool)
  val nickname: String? = null,
  val subnickname: String? = null,
  
  val created: ZonedDateTime? = null, // UTC+0
  val updated: ZonedDateTime? = null, // UTC+0
  val enabled: Boolean? = null,
  
  val firstName: String? = null,
  val lastName: String? = null,
  val birthDate: LocalDate? = null,
)