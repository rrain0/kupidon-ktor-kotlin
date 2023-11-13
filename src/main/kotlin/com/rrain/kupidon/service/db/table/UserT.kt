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
      UserTname,
      UserTbirthDate,
      UserTgender,
      UserTaboutMe,
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
object UserTname : Column() {
  init {
    name = """"name""""
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
object UserTgender : Column() {
  init {
    name = """"gender""""
    table = UserT
    type = String::class.java
  }
}
object UserTaboutMe : Column() {
  init {
    name = """"aboutMe""""
    table = UserT
    type = String::class.java
  }
}


