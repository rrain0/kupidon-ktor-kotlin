package com.rrain.kupidon.service.db.ARCHIVE.postgres.ARCHIVE.first

import com.rrain.kupidon.entity.app.Role
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class RoleDbService(private val database: Database) {
  
  object RoleT : Table(""""Role"""") {
    val role = varchar(""""role"""", 50)
    
    override val primaryKey = PrimaryKey(role)
  }
  
  init {
    transaction(database) {
      SchemaUtils.create(RoleT)
    }
  }
  
  suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
  
  suspend fun create(role: Role): String = dbQuery {
    RoleT.insert {
      it[RoleT.role] = role.toString()
    }[RoleT.role]
  }
  
  suspend fun createIgnore(role: Role): String = dbQuery {
    RoleT.insertIgnore {
      it[RoleT.role] = role.toString()
    }[RoleT.role]
  }
  
  suspend fun get(role: String): Role? {
    return dbQuery {
      RoleT.select { RoleT.role eq role }
        .map { Role.valueOf(it[RoleT.role]) }
        .singleOrNull()
    }
  }
  
  suspend fun getAll(): List<Role> {
    return dbQuery {
      RoleT.selectAll()
        .map { Role.valueOf(it[RoleT.role]) }
    }
  }
  
  suspend fun update(role: Role, roleNew: Role) {
    dbQuery {
      RoleT.update({ RoleT.role eq role.toString() }) {
        it[RoleT.role] = roleNew.toString()
      }
    }
  }
  
  suspend fun delete(role: Role) {
    dbQuery {
      RoleT.deleteWhere { RoleT.role.eq(role.toString()) }
    }
  }
}