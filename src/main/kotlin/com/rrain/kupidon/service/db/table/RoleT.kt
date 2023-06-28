package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.util.*


object RoleT : Table() {
  init {
    name = """"Role""""
    cols = listOf(
      RoleTid,
      RoleTrole,
    )
  }
}


object RoleTid : Column() {
  init {
    name = """"id""""
    table = RoleT
    type = UUID::class.java
  }
}
object RoleTrole : Column() {
  init {
    name = """"role""""
    table = RoleT
    type = String::class.java
  }
}
