package com.rrain.kupidon.examples.second

import com.rrain.kupidon.entity.app.User
import io.ktor.server.util.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestamp
import java.sql.ResultSet
import java.time.LocalDate
import java.time.ZonedDateTime



class UserDbService(private val database: Database) {
  
  
  @OptIn(InternalAPI::class)
  fun ResultSet.toUser() = User(
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
  fun getById(id: String): User? = transaction(database) {
    val data = mutableListOf<User>()
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