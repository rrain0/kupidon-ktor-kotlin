package com.rrain.kupidon.service.db.ARCHIVE.postgres.`table-service`

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import reactor.kotlin.core.publisher.toFlux
import java.time.ZonedDateTime


class TransactionDbService(val pool: ConnectionPool) {
  
  
  fun rowToObject(row: Row, rowMetadata: RowMetadata) = mapOf<String,Any?>(
    "id" to row["id", String::class.java],
    "type" to row["type", String::class.java],
    "created" to row["created", ZonedDateTime::class.java],
    "updated" to row["updated", ZonedDateTime::class.java],
    "bind" to row["bind", String::class.java]!!
      .let { jacksonObjectMapper().readValue<MutableMap<String,Any?>>(it) },
    "from" to row["from", String::class.java]!!
      .let { jacksonObjectMapper().readValue<MutableMap<String,Any?>>(it) },
    "to" to row["to", String::class.java]!!
      .let { jacksonObjectMapper().readValue<MutableMap<String,Any?>>(it) },
    "state" to row["state", String::class.java]!!
      .let { jacksonObjectMapper().readValue<MutableMap<String,Any?>>(it) },
  )
  
  
  suspend fun getAll(connection: Connection? = null): Flow<Map<String,Any?>> {
    val conn = connection ?: pool.create().awaitSingle()
    @Language("sql") val sql = """
      select
        "id","type","created","updated","bind","from","to","state"
      from "Transaction";
    """.trimIndent()
    return try {
      conn
        .createStatement(sql)
        .execute() // тут вернётся реактивный поток
        .toFlux() // который мы преобразуем в удобный Reactor Flux
        .flatMapSequential { it.map(::rowToObject) }
        .asFlow()
    } finally {
      if (connection==null) conn
        .close() // реактивно закроем соединение
        .awaitFirstOrNull() // и подождем null - мы же в корутине.
    }
  }
  
  
}