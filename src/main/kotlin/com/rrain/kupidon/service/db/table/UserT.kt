package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.db.`table-interface`.Column
import com.rrain.kupidon.service.db.`table-interface`.Table
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*



val UserTid = Column(
  dbName = """"id"""",
  name = "id",
  type = UUID::class.java,
)
val UserTemail = Column(
  dbName = """"email"""",
  name = "email",
  type = String::class.java,
)
val UserTemailVerified = Column(
  dbName = """"emailVerified"""",
  name = "emailVerified",
  type = Boolean::class.javaObjectType,
)
val UserTpwd = Column(
  dbName = """"pwd"""",
  name = "pwd",
  type = String::class.java,
)
val UserTcreated = Column(
  dbName = """"created"""",
  name = "created",
  type = ZonedDateTime::class.java,
)
val UserTupdated = Column(
  dbName = """"updated"""",
  name = "updated",
  type = ZonedDateTime::class.java,
)
val UserTenabled = Column(
  dbName = """"enabled"""",
  name = "enabled",
  type = Boolean::class.javaObjectType,
)
val UserTname = Column(
  dbName = """"name"""",
  name = "name",
  type = String::class.java,
)
val UserTbirthDate = Column(
  dbName = """"birthDate"""",
  name = "birthDate",
  type = LocalDate::class.java,
)
val UserTgender = Column(
  dbName = """"gender"""",
  name = "gender",
  type = String::class.java,
)
val UserTaboutMe = Column(
  dbName = """"aboutMe"""",
  name = "aboutMe",
  type = String::class.java,
)


val UserT = Table(
  dbName = """"User"""",
  name = "User",
).apply {
    setColumns(listOf(
      UserTid,
      UserTemail,
      UserTemailVerified,
      UserTpwd,
      UserTcreated,
      UserTupdated,
      UserTenabled,
      UserTname,
      UserTbirthDate,
      UserTgender,
      UserTaboutMe,
    ))
}


