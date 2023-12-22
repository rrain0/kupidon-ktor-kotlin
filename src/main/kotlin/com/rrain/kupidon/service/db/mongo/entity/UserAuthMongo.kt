package com.rrain.kupidon.service.db.mongo.entity

import com.rrain.kupidon.entity.app.Role
import java.util.UUID




data class UserAuthMongo(
  // string UUID
  // e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val id: UUID,
  
  val roles: Set<Role>,
  
  // login can be email, phone, nickname#subnickname
  val email: String,
  
  // hashed password
  val pwd: String,
)
