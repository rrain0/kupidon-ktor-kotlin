package com.rrain.kupidon.examples

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
import java.time.LocalDate
import java.time.ZonedDateTime



class UserDbService(private val database: Database) {
  
  object UserT : Table(""""User"""") {
    val id = uuid(""""id"""")
    
    val email = varchar(""""email"""",50)
    val pwd = varchar(""""pwd"""",200)
    
    val nickname = varchar(""""nickname"""", 50)
    val subnickname = varchar(""""subnickname"""", 50)
    
    val firstName = varchar(""""firstName"""",100)
    val lastName = varchar(""""lastName"""",100)
    val birthDate = timestamp(""""birthDate"""")
    
    val created = timestamp(""""created"""")
    val updated = timestamp(""""updated"""")
    
    override val primaryKey = PrimaryKey(id)
  }
  
  init {
    transaction(database) {
      SchemaUtils.create(UserT)
    }
  }
  
  suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
  
  /*suspend fun create(role: Role): String = dbQuery {
    UserT.insert {
      it[UserT.role] = role.role
    }[UserT.role]
  }*/
  
  /*suspend fun createIgnore(role: Role): String = dbQuery {
    UserT.insertIgnore {
      it[UserT.role] = role.role
    }[UserT.role]
  }*/
  
  /*suspend fun get(role: String): Role? {
    return dbQuery {
      UserT.select { UserT.role eq role }
        .map { Role(it[UserT.role]) }
        .singleOrNull()
    }
  }*/
  
  /*suspend fun getAll(): List<Role> {
    return dbQuery {
      UserT.selectAll()
        .map { Role(it[UserT.role]) }
    }
  }*/
  
  /*suspend fun update(role: Role) {
    dbQuery {
      UserT.update({ UserT.role eq role.role }) {
        it[UserT.role] = role.role
      }
    }
  }*/
  
  /*suspend fun delete(role: Role) {
    dbQuery {
      UserT.deleteWhere { UserT.role.eq(role.role) }
    }
  }*/
  
}