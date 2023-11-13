package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.util.*


object UserRoleT : Table() {
  init {
    dbName = """"UserRole""""
    cols = listOf(
      UserRoleTuserId,
      UserRoleTroleId,
    )
  }
}


object UserRoleTuserId : Column() {
  init {
    dbName = """"userId""""
    table = UserRoleT
    type = UUID::class.java
  }
}
object UserRoleTroleId : Column() {
  init {
    dbName = """"roleId""""
    table = UserRoleT
    type = UUID::class.java
  }
}
