package com.rrain.kupidon.service.table




abstract class Table {
  lateinit var name: String
  var cols: List<Column> = listOf()
  
  fun allColNames() = cols
    .map { it.name }
    .joinToString(",\n")
  
  fun allColsAs(vararg except: Column) = cols
    .filter { it !in except }
    .map { it.tableColAs() }
    .joinToString(",\n")
  
  fun allColsAggArrNoNulls() = cols
    .map { it.aggArrNoNulls() }
    .joinToString(",\n")
}

