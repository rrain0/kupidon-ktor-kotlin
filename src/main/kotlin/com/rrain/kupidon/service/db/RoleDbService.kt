package com.rrain.kupidon.service.db

import com.rrain.kupidon.entity.app.Role
import io.r2dbc.pool.ConnectionPool
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.*
import reactor.kotlin.core.publisher.toFlux


class RoleDbService(private val pool: ConnectionPool) {
  
  suspend fun exists(role: String): Boolean {
    val connection = pool.create().awaitSingle()
    @Language("sql") val sql = """
      select count(1) from "Role" where "role" = '$role';
    """.trimIndent()
    return try {
      connection
        .createStatement(sql)
        .execute() // тут вернётся реактивный поток
        .awaitSingle() // подождем, пока все соберется - мы же в корутине.
        .map { row, rowMetadata ->
          1L == row.get(0, java.lang.Long::class.java)!!.toLong()
        }
        .awaitSingle()
    } finally {
      connection
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
  suspend fun getAll(): Flow<Role> {
    val connection = pool.create().awaitSingle()
    @Language("sql") val sql = """
      select "Role"."role" as "Role.role" from "Role";
    """.trimIndent()
    return try {
      connection
        .createStatement(sql)
        .execute() // тут вернётся реактивный поток
        .toFlux() // который мы преобразуем в удобный Reactor Flux
        .flatMapSequential { // один результат может породить несколько записей. Как - это дело драйвера, мы только принимаем факт
          it.map { row, rowMetadata -> // преобразуем данные в поток объектов
            row["Role.role", String::class.java].let { Role.valueOf(it) }
          }
        }
        .asFlow()
    } finally {
      connection
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
}