package com.rrain.kupidon.service.table

import org.intellij.lang.annotations.Language


abstract class Column {
  lateinit var name: String
  lateinit var table: Table
  lateinit var type: Class<*>
  
  
  
  // "User"."id"
  @Language("sql")
  fun tableDotCol() = """${table.name}.${name}"""
  
  // "User.id"
  @Language("sql")
  fun tableJoinCol() = """${table.name.dropLast(1)}.${name.drop(1)}"""
  
  // User.id
  @Language("sql")
  fun tableJoinColNoQuotes() = """${table.name.drop(1).dropLast(1)}.${name.drop(1).dropLast(1)}"""
  
  // User.idArr
  @Language("sql")
  fun tableJoinColArrNoQuotes() = """${tableJoinColNoQuotes()}Arr"""
  
  // "User.idArr"
  @Language("sql")
  fun tableJoinColArr() = """"${tableJoinColArrNoQuotes()}""""
  
  
  
  // "User"."id" as "User.id"
  @Language("sql")
  fun tableColAs() = """${tableDotCol()} as ${tableJoinCol()}"""
  
  // array_remove(array_agg("User"."id"),null) as "User.idArr"
  @Language("sql")
  fun aggArrNoNulls() = """array_remove(array_agg(${tableDotCol()}),null) as ${tableJoinColArr()}"""
  
}
