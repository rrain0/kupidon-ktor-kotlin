package com.rrain.kupidon.service.db.`table-service`

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.service.db.mappings.bindList
import com.rrain.kupidon.service.db.mappings.toSqlBind
import com.rrain.kupidon.service.db.table.*
import com.rrain.kupidon.service.db.`table-interface`.Column
import com.rrain.kupidon.util.cast
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*



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
    
    name = row[UserTname.tableJoinColNoQuotes(), String::class.java],
    birthDate = row[UserTbirthDate.tableJoinColNoQuotes(), LocalDate::class.java],
    gender = row[UserTgender.tableJoinColNoQuotes(), String::class.java]?.let(Gender::valueOf),
    aboutMe = row[UserTaboutMe.tableJoinColNoQuotes(), String::class.java],
  )
  
  fun rowToCreatedUser(row: Row, rowMetadata: RowMetadata) = User(
    id = row[UserTid.tableJoinColNoQuotes(), String::class.java],
    
    email = row[UserTemail.tableJoinColNoQuotes(), String::class.java],
    emailVerified = row[UserTemailVerified.tableJoinColNoQuotes(), Boolean::class.javaObjectType],
    pwd = row[UserTpwd.tableJoinColNoQuotes(), String::class.java],
    
    created = row[UserTcreated.tableJoinColNoQuotes(), ZonedDateTime::class.java],
    updated = row[UserTupdated.tableJoinColNoQuotes(), ZonedDateTime::class.java],
    enabled = row[UserTenabled.tableJoinColNoQuotes(), Boolean::class.javaObjectType],
    
    name = row[UserTname.tableJoinColNoQuotes(), String::class.java],
    birthDate = row[UserTbirthDate.tableJoinColNoQuotes(), LocalDate::class.java],
    gender = row[UserTgender.tableJoinColNoQuotes(), String::class.java]?.let(Gender::valueOf),
    aboutMe = row[UserTaboutMe.tableJoinColNoQuotes(), String::class.java],
  )
  
  
  
  suspend fun getById(id: String, connection: Connection? = null): User? {
    val conn = connection ?: pool.create().awaitSingle()
    @Language("sql") val sql = """
      select
        ${UserT.allColsAs()},
        ${RoleT.allColsAggArrNoNulls()}
      from ${UserT.dbName}
      left join ${UserRoleT.dbName} on ${UserTid.tableDotCol()} = ${UserRoleTuserId.tableDotCol()}
      left join ${RoleT.dbName} on ${RoleTid.tableDotCol()} = ${UserRoleTroleId.tableDotCol()}
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
      from ${UserT.dbName}
      left join ${UserRoleT.dbName} on ${UserTid.tableDotCol()} = ${UserRoleTuserId.tableDotCol()}
      left join ${RoleT.dbName} on ${RoleTid.tableDotCol()} = ${UserRoleTroleId.tableDotCol()}
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
        insert into ${UserT.dbName} (
          ${UserTemail.dbName},
          ${UserTpwd.dbName},
          
          ${UserTcreated.dbName},
          ${UserTupdated.dbName},
          
          ${UserTname.dbName},
          ${UserTgender.dbName},
          ${UserTbirthDate.dbName}
        ) values (
          $1,
          $2,
          
          transaction_timestamp(),
          transaction_timestamp(),
          
          $3,
          $4,
          $5
        ) returning ${UserT.allColsAs()}
      """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .bind("$1",user.email!!)
        .bind("$2",user.pwd!!)
        .bind("$3",user.name!!)
        .bind("$4",user.gender!!.name)
        .bind("$5",user.birthDate!!)
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
    val vs = values.toList()
    val idIdx = vs.size+1
    @Language("sql") val sql = """
        update ${UserT.dbName} set
        ${vs.toSqlBind()}
        where ${UserTid.tableDotCol()} = $$idIdx
        returning ${UserT.allColsAs()}
      """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .apply { println("vs: $vs") }
        .bindList(vs)
        .bind("$$idIdx", UUID.fromString(id))
        .execute()
        .awaitSingle()
        .map(::rowToCreatedUser)
        .awaitSingle()
    } finally {
      if (connection==null) conn.close().awaitFirstOrNull()
    }
  }
  
  
}