package com.rrain.kupidon.service.db

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.entity.app.User
import com.rrain.kupidon.util.cast
import io.ktor.server.util.*
import io.ktor.util.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.ZonedDateTime



class UserDbService(private val pool: ConnectionPool) {
  
  fun rowToUser(row: Row, rowMetadata: RowMetadata) = User(
    id = row["User.id", String::class.java],
    
    email = row["User.email", String::class.java],
    pwd = row["User.pwd", String::class.java],
    
    roles = row["UserRole.roles"].cast<Array<String?>>()
      .apply { println("array: ${this.toList()}") }
      .filterNotNull()
      .map { Role.valueOf(it) }
      .toSet(),
    
    nickname = row["User.nickname", String::class.java],
    subnickname = row["User.subnickname", String::class.java],
    
    created = row["User.created", ZonedDateTime::class.java],
    updated = row["User.updated", ZonedDateTime::class.java],
    enabled = row["User.enabled"].cast<Boolean>(),
    
    firstName = row["User.firstName", String::class.java],
    lastName = row["User.lastName", String::class.java],
    birthDate = row["User.birthDate", LocalDate::class.java],
  )
  
  suspend fun getById(id: String): User? {
    val connection = pool.create().awaitSingle()
    @Language("sql") val sql = """
      select
        "User"."id" as "User.id", "User"."email" as "User.email", "User"."pwd" as "User.pwd",
        array_agg("UserRole"."role") as "UserRole.roles",
        "User"."nickname" as "User.nickname", "User"."subnickname" as "User.subnickname",
        "User"."created" as "User.created", "User"."updated" as "User.updated", "User"."enabled" as "User.enabled",
        "User"."firstName" as "User.firstName", "User"."lastName" as "User.lastName",
        "User"."birthDate" as "User.birthDate"
      from "User"
      left join "UserRole" on "User"."id" = "UserRole"."userId"
	    where "User"."id" = uuid '$id'
	    group by "User"."id"
      ;
    """.trimIndent()
    return try {
      connection
        .createStatement(sql)
        .execute() // тут вернётся реактивный поток
        .awaitFirstOrNull() // подождем, пока все соберется - мы же в корутине.
        ?.map(::rowToUser)
        ?.awaitFirstOrNull()
    } finally {
      connection
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  suspend fun getByEmail(email: String): User? {
    val connection = pool.create().awaitSingle()
    @Language("sql") val sql = """
      select
        "User"."id" as "User.id", "User"."email" as "User.email", "User"."pwd" as "User.pwd",
        array_agg("UserRole"."role") as "UserRole.roles",
        "User"."nickname" as "User.nickname", "User"."subnickname" as "User.subnickname",
        "User"."created" as "User.created", "User"."updated" as "User.updated", "User"."enabled" as "User.enabled",
        "User"."firstName" as "User.firstName", "User"."lastName" as "User.lastName",
        "User"."birthDate" as "User.birthDate"
      from "User"
      left join "UserRole" on "User"."id" = "UserRole"."userId"
	    where "User"."email" = '$email'
	    group by "User"."id", "User"."email"
      ;
    """.trimIndent()
    return try {
      connection
        .createStatement(sql)
        .execute() // тут вернётся реактивный поток
        .awaitFirstOrNull() // подождем, пока все соберется - мы же в корутине.
        ?.map(::rowToUser)
        ?.awaitFirstOrNull()
    } finally {
      connection
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
}