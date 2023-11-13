package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*


object UserT : Table() {
  init {
    dbName = """"User""""
    name = "User"
    cols = listOf(
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
    )
  }
}


object UserTid : Column() {
  init {
    dbName = """"id""""
    table = UserT
    type = UUID::class.java
  }
}
object UserTemail : Column() {
  init {
    dbName = """"email""""
    table = UserT
    type = String::class.java
  }
}
object UserTemailVerified : Column() {
  init {
    dbName = """"emailVerified""""
    table = UserT
    type = Boolean::class.javaObjectType
  }
}
object UserTpwd : Column() {
  init {
    dbName = """"pwd""""
    table = UserT
    type = String::class.java
  }
}
object UserTcreated : Column() {
  init {
    dbName = """"created""""
    table = UserT
    type = ZonedDateTime::class.java
  }
}
object UserTupdated : Column() {
  init {
    dbName = """"updated""""
    table = UserT
    type = ZonedDateTime::class.java
  }
}
object UserTenabled : Column() {
  init {
    dbName = """"enabled""""
    table = UserT
    type = Boolean::class.javaObjectType
  }
}
object UserTname : Column() {
  init {
    dbName = """"name""""
    table = UserT
    type = String::class.java
  }
}
object UserTbirthDate : Column() {
  init {
    dbName = """"birthDate""""
    table = UserT
    type = LocalDate::class.java
  }
}
object UserTgender : Column() {
  init {
    dbName = """"gender""""
    table = UserT
    type = String::class.java
  }
}
object UserTaboutMe : Column() {
  init {
    dbName = """"aboutMe""""
    table = UserT
    type = String::class.java
  }
}


