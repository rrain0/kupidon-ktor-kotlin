package com.rrain.kupidon.service.db.`table-interface`




data class Table(
  val dbName: String,
  val name: String,
) {
  lateinit var cols: List<Column>
  
  fun setColumns(cols: List<Column>){
    this.cols = cols
    this.cols.forEach{
      it.table = this
    }
  }
  
  fun allColNames() = cols
    .map { it.dbName }
    .joinToString(",\n")
  
  fun allColsAs(vararg except: Column) = cols
    .filter { it !in except }
    .map { it.tableColAs() }
    .joinToString(",\n")
  
  fun allColsAggArrNoNulls() = cols
    .map { it.aggArrNoNulls() }
    .joinToString(",\n")
}

