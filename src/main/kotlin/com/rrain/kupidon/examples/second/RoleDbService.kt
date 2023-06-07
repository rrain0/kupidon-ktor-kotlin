package com.rrain.kupidon.examples.second

import com.rrain.kupidon.entity.app.Role
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.coroutines.CoroutineContext


class RoleDbService(private val database: Database) {
  
  
  
  
  
  suspend fun <T> dbQuery(
    block: suspend Transaction.(context: CoroutineContext) -> T
  ): T = newSuspendedTransaction(Dispatchers.IO, database) { block(Dispatchers.IO) }
  
  /*suspend fun create(role: Role): String = dbQuery {
    RoleT.insert {
      it[RoleT.role] = role.toString()
    }[RoleT.role]
  }*/
  
  /*suspend fun createIgnore(role: Role): String = dbQuery {
    RoleT.insertIgnore {
      it[RoleT.role] = role.toString()
    }[RoleT.role]
  }*/
  
  /*suspend fun get(role: String): Role? {
    return dbQuery {
      RoleT.select { RoleT.role eq role }
        .map { Role.valueOf(it[RoleT.role]) }
        .singleOrNull()
    }
  }*/
  
  fun exists(role: String): Boolean = transaction(database) {
    exec("""
        select count(1) from "Role" where "role" = '$role';
      """.trimIndent()){
      it.next()
      1==it.getInt(1)
    }!!
  }
  
  fun getAll(): List<Role> = transaction(database) {
    exec("""select "Role"."role" as "role" from "Role";"""){
      val list = mutableListOf<Role>()
      while (it.next()){
        list += Role.valueOf(it.getString("role"))
      }
      list
    }!!
  }
  
  /*suspend fun update(role: Role, roleNew: Role) {
    dbQuery {
      RoleT.update({ RoleT.role eq role.toString() }) {
        it[RoleT.role] = roleNew.toString()
      }
    }
  }*/
  
  /*suspend fun delete(role: Role) {
    dbQuery {
      RoleT.deleteWhere { RoleT.role.eq(role.toString()) }
    }
  }*/
}