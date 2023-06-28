package com.rrain.kupidon.service.db.table

import com.rrain.kupidon.service.table.Column
import com.rrain.kupidon.service.table.Table
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*


object UserT : Table() {
  init {
    name = """"User""""
    cols = listOf(
      UserTid,
      UserTemail,
      UserTemailVerified,
      UserTpwd,
      UserTcreated,
      UserTupdated,
      UserTenabled,
      UserTfirstName,
      UserTlastName,
      UserTbirthDate,
      UserTsex
    )
  }
}


object UserTid : Column() {
  init {
    name = """"id""""
    table = UserT
    type = UUID::class.java
  }
}
object UserTemail : Column() {
  init {
    name = """"email""""
    table = UserT
    type = String::class.java
  }
}
object UserTemailVerified : Column() {
  init {
    name = """"emailVerified""""
    table = UserT
    type = Boolean::class.javaObjectType
  }
}
object UserTpwd : Column() {
  init {
    name = """"pwd""""
    table = UserT
    type = String::class.java
  }
}
object UserTcreated : Column() {
  init {
    name = """"created""""
    table = UserT
    type = ZonedDateTime::class.java
  }
}
object UserTupdated : Column() {
  init {
    name = """"updated""""
    table = UserT
    type = ZonedDateTime::class.java
  }
}
object UserTenabled : Column() {
  init {
    name = """"enabled""""
    table = UserT
    type = Boolean::class.javaObjectType
  }
}
object UserTfirstName : Column() {
  init {
    name = """"firstName""""
    table = UserT
    type = String::class.java
  }
}
object UserTlastName : Column() {
  init {
    name = """"lastName""""
    table = UserT
    type = String::class.java
  }
}
object UserTbirthDate : Column() {
  init {
    name = """"birthDate""""
    table = UserT
    type = LocalDate::class.java
  }
}
object UserTsex : Column() {
  init {
    name = """"sex""""
    table = UserT
    type = String::class.java
  }
}


