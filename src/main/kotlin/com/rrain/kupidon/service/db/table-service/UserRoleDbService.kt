package com.rrain.kupidon.service.db.`table-service`

import com.rrain.kupidon.entity.app.Role
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID


data class UserRoleDb(
  var userId: String?,
  var role: Role?,
)
class UserRoleDbService(private val database: Database) {
  
  object UserRoleT : Table(""""UserRole"""") {
    val userId = reference(""""userId"""", com.rrain.kupidon.archive.UserDbService.UserT.id)
    val role = reference(""""role"""", com.rrain.kupidon.archive.RoleDbService.RoleT.role)
    
    override val primaryKey = PrimaryKey(userId, role)
  }
  
  init {
    transaction(database){
      SchemaUtils.create(UserRoleT)
    }
  }
  
  suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
  
  suspend fun create(userRoleDb: UserRoleDb): UserRoleDb = dbQuery {
    val insertStatement = UserRoleT.insert {
      it[userId] = userRoleDb.userId.run(UUID::fromString)
      it[role] = userRoleDb.role.toString()
    }
    insertStatement.let { UserRoleT.run {
      UserRoleDb(it[userId].toString(), it[role].run(Role::valueOf))
    } }
  }
  
  suspend fun createIgnore(userRoleDb: UserRoleDb): Unit = dbQuery {
    val insertStatement = UserRoleT.insertIgnore {
      it[userId] = userRoleDb.userId.run(UUID::fromString)
      it[role] = userRoleDb.role.toString()
    }
    insertStatement.let { UserRoleT.run {
      UserRoleDb(it[userId].toString(), it[role].run(Role::valueOf))
    } }
  }
  
  suspend fun get(userRoleDb: UserRoleDb): UserRoleDb? = dbQuery {
    UserRoleT.select {
      UserRoleT.userId eq userRoleDb.userId.run(UUID::fromString) and
      (UserRoleT.role eq userRoleDb.role.toString())
    }
      .map { UserRoleDb(
        it[UserRoleT.userId].toString(),
        it[UserRoleT.role].run(Role::valueOf)
      ) }
      .singleOrNull()
  }
  
  suspend fun getAll(): List<UserRoleDb> = dbQuery {
    UserRoleT.selectAll()
      .map { UserRoleDb(
        it[UserRoleT.userId].toString(),
        it[UserRoleT.role].run(Role::valueOf)
      ) }
  }
  
  suspend fun update(userRoleDb: UserRoleDb): Unit = dbQuery {
    UserRoleT.update({
      UserRoleT.userId eq userRoleDb.userId.run(UUID::fromString) and
      (UserRoleT.role eq userRoleDb.role.toString())
    }) {
      it[userId] = userRoleDb.userId.run(UUID::fromString)
      it[role] = userRoleDb.role.toString()
    }
  }
  
  suspend fun delete(userRoleDb: UserRoleDb): Unit = dbQuery {
    com.rrain.kupidon.archive.UserDbService.UserT.deleteWhere {
      UserRoleT.userId eq userRoleDb.userId.run(UUID::fromString) and
      (UserRoleT.role eq userRoleDb.role.toString())
    }
  }
  
}