package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.util.*





val RoleTid = Column(
  dbName = """"id"""",
  name = "id",
  type = UUID::class.java,
)
val RoleTrole = Column(
  dbName = """"role"""",
  name = "role",
  type = String::class.java,
)



val RoleT = Table(
  dbName = """"Role"""",
  name = "Role",
).apply {
  setColumns(listOf(
    RoleTid,
    RoleTrole,
  ))
}


