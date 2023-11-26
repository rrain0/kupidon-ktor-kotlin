package com.rrain.kupidon._old.v02.postgres.service.db

import io.ktor.server.util.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import java.sql.ResultSet


class UserDbService(private val database: Database) {
  
  
  @OptIn(InternalAPI::class)
  fun ResultSet.toUser() = User0(
    id = this.getString(1),
    
    email = this.getString(2),
    pwd = this.getString(3),
    
    // roles = ,
    
    nickname = this.getString(4),
    subnickname = this.getString(5),
    
    created = this.getTimestamp(6).toZonedDateTime(),
    updated = this.getTimestamp(7).toZonedDateTime(),
    enabled = this.getBoolean(8),
    
    firstName = this.getString(9),
    lastName = this.getString(10),
    birthDate = this.getDate(11).toLocalDate(),
  )

  
  
  // todo if wrong uuid string postgres throws error
  fun getById(id: String): User0? = transaction(database) {
    val data = mutableListOf<User0>()
    exec("""
        select * from "User" where "id" = uuid '$id';
    """.trimIndent()){
      if (it.next()){
        data += it.toUser()
      }
    }
    if (data.size==1) data[0] else null
  }
  
  
  
}