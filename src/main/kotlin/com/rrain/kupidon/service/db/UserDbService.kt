package com.rrain.kupidon.service.db

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.entity.app.Sex
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.service.db.mappings.bindList
import com.rrain.kupidon.service.db.mappings.toSql
import com.rrain.kupidon.service.db.mappings.toSqlBind
import com.rrain.kupidon.service.db.table.*
import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.util.cast
import com.rrain.kupidon.util.localDateFormat
import io.ktor.server.util.*
import io.ktor.util.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance


class UserDbService(val pool: ConnectionPool) {
  
  fun rowToUser(row: Row, rowMetadata: RowMetadata) = User(
    id = row[UserTid.tableJoinColNoQuotes(), String::class.java],
    
    email = row[UserTemail.tableJoinColNoQuotes(), String::class.java],
    emailVerified = row[UserTemailVerified.tableJoinColNoQuotes(), Boolean::class.javaObjectType],
    pwd = row[UserTpwd.tableJoinColNoQuotes(), String::class.java],
    
    roles = row[RoleTrole.tableJoinColArrNoQuotes()].cast<Array<String>>()
      .map(Role::valueOf)
      .toSet(),
    
    created = row[UserTcreated.tableJoinColNoQuotes(), ZonedDateTime::class.java],
    updated = row[UserTupdated.tableJoinColNoQuotes(), ZonedDateTime::class.java],
    enabled = row[UserTenabled.tableJoinColNoQuotes(), Boolean::class.javaObjectType],
    
    firstName = row[UserTfirstName.tableJoinColNoQuotes(), String::class.java],
    lastName = row[UserTlastName.tableJoinColNoQuotes(), String::class.java],
    birthDate = row[UserTbirthDate.tableJoinColNoQuotes(), LocalDate::class.java],
    sex = row[UserTsex.tableJoinColNoQuotes(), String::class.java]?.let(Sex::valueOf),
  )
  
  fun rowToCreatedUser(row: Row, rowMetadata: RowMetadata) = User(
    id = row[UserTid.tableJoinColNoQuotes(), String::class.java],
    
    email = row[UserTemail.tableJoinColNoQuotes(), String::class.java],
    emailVerified = row[UserTemailVerified.tableJoinColNoQuotes(), Boolean::class.javaObjectType],
    pwd = row[UserTpwd.tableJoinColNoQuotes(), String::class.java],
    
    created = row[UserTcreated.tableJoinColNoQuotes(), ZonedDateTime::class.java],
    updated = row[UserTupdated.tableJoinColNoQuotes(), ZonedDateTime::class.java],
    enabled = row[UserTenabled.tableJoinColNoQuotes(), Boolean::class.javaObjectType],
    
    firstName = row[UserTfirstName.tableJoinColNoQuotes(), String::class.java],
    lastName = row[UserTlastName.tableJoinColNoQuotes(), String::class.java],
    birthDate = row[UserTbirthDate.tableJoinColNoQuotes(), LocalDate::class.java],
    sex = row[UserTsex.tableJoinColNoQuotes(), String::class.java]?.let(Sex::valueOf),
  )
  
  
  suspend fun getById(id: String, connection: Connection? = null): User? {
    val conn = connection ?: pool.create().awaitSingle()
    @Language("sql") val sql = """
      select
        ${UserT.allColsAs()},
        ${RoleT.allColsAggArrNoNulls()}
      from ${UserT.name}
      left join ${UserRoleT.name} on ${UserTid.tableDotCol()} = ${UserRoleTuserId.tableDotCol()}
      left join ${RoleT.name} on ${RoleTid.tableDotCol()} = ${UserRoleTroleId.tableDotCol()}
	    where ${UserTid.tableDotCol()} = $1
	    group by ${UserTid.tableDotCol()}
    """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .bind("$1",UUID.fromString(id))
        .execute() // тут вернётся реактивный поток
        .awaitFirstOrNull() // подождем, пока все соберется - мы же в корутине.
        ?.map(::rowToUser)
        ?.awaitFirstOrNull()
    } finally {
      if (connection==null) conn
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
  suspend fun getByEmail(email: String, connection: Connection? = null): User? {
    val conn = connection ?: pool.create().awaitSingle()
    @Language("sql") val sql = """
      select
        ${UserT.allColsAs()},
        ${RoleT.allColsAggArrNoNulls()}
      from ${UserT.name}
      left join ${UserRoleT.name} on ${UserTid.tableDotCol()} = ${UserRoleTuserId.tableDotCol()}
      left join ${RoleT.name} on ${RoleTid.tableDotCol()} = ${UserRoleTroleId.tableDotCol()}
	    where ${UserTemail.tableDotCol()} = $1
	    group by ${UserTid.tableDotCol()}
    """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .bind(0,email)
        .execute() // тут вернётся реактивный поток
        .awaitFirstOrNull() // подождем, пока все соберется - мы же в корутине.
        ?.map(::rowToUser)
        ?.awaitFirstOrNull()
    } finally {
      if (connection==null) conn
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
  suspend fun create(user: User, connection: Connection? = null): User {
    val conn = connection ?: pool.create().awaitSingle()
      @Language("sql") val sql = """
        insert into ${UserT.name} (
          ${UserTemail.name},
          ${UserTpwd.name},
          
          ${UserTcreated.name},
          ${UserTupdated.name},
          
          ${UserTfirstName.name},
          ${UserTlastName.name},
          ${UserTsex.name},
          ${UserTbirthDate.name}
        ) values (
          $1,
          $2,
          
          transaction_timestamp(),
          transaction_timestamp(),
          
          $3,
          $4,
          $5,
          $6
        ) returning ${UserT.allColsAs()}
      """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .bind("$1",user.email!!)
        .bind("$2",user.pwd!!.let(PwdHashing::generateHash))
        .bind("$3",user.firstName!!)
        .bind("$4",user.lastName!!)
        .bind("$5",user.sex!!.name)
        .bind("$6",user.birthDate!!)
        .execute()
        .awaitSingle()
        .map(::rowToCreatedUser)
        .awaitSingle()
    } finally {
      if (connection==null) conn.close().awaitFirstOrNull()
    }
    
  }
  
  
  suspend fun update(id: String, values: Map<Column,Any?>, connection: Connection? = null): User {
    val conn = connection ?: pool.create().awaitSingle()
    val values = values.toList()
    @Language("sql") val sql = """
        update ${UserT.name} set
        ${values.toSqlBind()}
        where ${UserTid.tableDotCol()} = $${values.size+1}
        returning ${UserT.allColsAs()}
      """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .bindList(values)
        .bind("$${values.size+1}", UUID.fromString(id))
        .execute()
        .awaitSingle()
        .map(::rowToCreatedUser)
        .awaitSingle()
    } finally {
      if (connection==null) conn.close().awaitFirstOrNull()
    }
  }
  
  
}