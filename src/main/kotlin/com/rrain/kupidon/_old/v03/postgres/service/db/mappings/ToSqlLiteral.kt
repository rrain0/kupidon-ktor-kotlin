package com.rrain.kupidon._old.v03.postgres.service.db.mappings

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon._old.v03.postgres.service.db.`table-interface`.Column
import com.rrain.kupidon.util.*
import io.r2dbc.spi.Statement
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID


fun main(){
  listOf<Any?>(
    null,
    true,
    false.cast<Boolean?>(),
    "some string",
    "some string?".cast<String?>(),
    Role.ADMIN,
    "8d1541cb-f2d0-4395-908c-a14a19097f76".toUuid(),
    "2000-05-22".toLocalDate(),
    "2023-06-02 18:19:07.186+0800".toZonedDateTime(),
  ).forEach {
    println("$it: ${it.toSqlLiteral()}")
  }
}


fun Any?.toSqlLiteral(): String {
  return when (this){
    null -> "null"
    true -> "true"; false -> "false"
    is String -> this
    is Enum<*> -> this.name
    is UUID -> "uuid'$this'"
    is LocalDate -> "date'${this.format(localDateFormat)}'"
    is ZonedDateTime -> "timestamptz'${this.format(zonedDateTimeFormatter)}'"
    else -> throw UnsupportedOperationException("Can't find appropriate SQL literal for provided data type")
  }
}
fun Any.toSqlBind(): Any {
  return when (this){
    is Enum<*> -> this.name
    else -> this
  }
}



fun Map<Column,Any?>.toSql(): String {
  return this
    .map { (k,v) -> "${k.dbName}=${v.toSqlLiteral()}" }
    .joinToString(",\n")
}


/*
* makes list <column-name>=<column-index>
* "name"=$1
* "age"=$2
* ...
* */
fun List<Pair<Column,*>>.toSqlBind(startIndex: Int = 0): String {
  return this
    .mapIndexed { i,(k,_) ->
      val idx = startIndex + i + 1
      "${k.dbName}=$$idx"
    }
    .joinToString(",\n")
}
fun Statement.bindList(list: List<Pair<Column,Any?>>, startIndex: Int = 0): Statement {
  list.forEachIndexed { i, (col,v) ->
    val idx = startIndex + i + 1
    if (v!=null) this.bind("$$idx", v.toSqlBind())
    else this.bindNull("$$idx",col.type)
  }
  return this
}