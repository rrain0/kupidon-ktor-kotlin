package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.util.*



val UserRoleTuserId = Column(
  dbName = """"userId"""",
  name = "userId",
  type = UUID::class.java,
)
val UserRoleTroleId = Column(
  dbName = """"roleId"""",
  name = "roleId",
  type = UUID::class.java,
)


val UserRoleT = Table(
  dbName = """"UserRole"""",
  name = "UserRole",
).apply {
  setColumns(listOf(
    UserRoleTuserId,
    UserRoleTroleId,
  ))
}

