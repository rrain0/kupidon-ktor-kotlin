package com.rrain.kupidon._old.v03.postgres.service.db.table

import com.rrain.kupidon._old.v03.postgres.service.db.`table-interface`.Column
import com.rrain.kupidon._old.v03.postgres.service.db.`table-interface`.Table
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

