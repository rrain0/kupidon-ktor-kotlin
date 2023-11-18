package com.rrain.kupidon.service.db.`table-interface`


data class Column(
  val dbName: String,
  val name: String,
  val type: Class<*>
) {
  lateinit var table: Table
  
  
  // "User"."id"
  fun tableDotCol() = """${table.dbName}.${dbName}"""
  
  // "User.id"
  fun tableJoinCol() = """${table.dbName.dropLast(1)}.${dbName.drop(1)}"""
  
  // User.id
  fun tableJoinColNoQuotes() = """${table.dbName.drop(1).dropLast(1)}.${dbName.drop(1).dropLast(1)}"""
  
  // User.idArr
  fun tableJoinColArrNoQuotes() = """${tableJoinColNoQuotes()}Arr"""
  
  // "User.idArr"
  fun tableJoinColArr() = """"${tableJoinColArrNoQuotes()}""""
  
  
  
  // "User"."id" as "User.id"
  fun tableColAs() = """${tableDotCol()} as ${tableJoinCol()}"""
  
  // array_remove(array_agg("User"."id"),null) as "User.idArr"
  fun aggArrNoNulls() = """array_remove(array_agg(${tableDotCol()}),null) as ${tableJoinColArr()}"""
  
}
