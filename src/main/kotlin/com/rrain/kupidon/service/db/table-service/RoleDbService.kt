package com.rrain.kupidon.service.db.`table-service`

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.service.db.table.RoleT
import com.rrain.kupidon.service.db.table.RoleTrole
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import reactor.kotlin.core.publisher.toFlux


class RoleDbService(val pool: ConnectionPool) {
  
  suspend fun exists(role: String, connection: Connection? = null): Boolean {
    val conn = connection ?: pool.create().awaitSingle()
    @Language("sql") val sql = """
      select count(1) from ${RoleT.dbName} where ${RoleTrole.dbName} = $1
    """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .bind("$1",role)
        .execute() // тут вернётся реактивный поток
        .awaitSingle() // подождем, пока все соберется - мы же в корутине.
        .map { row, rowMetadata ->
          1L == row.get(0, java.lang.Long::class.java)!!.toLong()
        }
        .awaitSingle()
    } finally {
      if (connection==null) conn
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
  suspend fun getAll(connection: Connection? = null): Flow<Role> {
    val conn = connection ?: pool.create().awaitSingle()
    @Language("sql") val sql = """
      select ${RoleT.allColsAs()} from ${RoleT.dbName}
    """.trimIndent()
    return try {
      conn
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
      if (connection==null) conn
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
}