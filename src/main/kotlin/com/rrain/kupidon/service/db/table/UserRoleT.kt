package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.util.*


object UserRoleT : Table() {
  init {
    name = """"UserRole""""
    cols = listOf(
      UserRoleTuserId,
      UserRoleTroleId,
    )
  }
}


object UserRoleTuserId : Column() {
  init {
    name = """"userId""""
    table = UserRoleT
    type = UUID::class.java
  }
}
object UserRoleTroleId : Column() {
  init {
    name = """"roleId""""
    table = UserRoleT
    type = UUID::class.java
  }
}
