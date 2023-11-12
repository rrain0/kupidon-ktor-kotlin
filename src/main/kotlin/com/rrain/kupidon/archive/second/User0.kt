package com.rrain.kupidon.archive.second

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.entity.app.Gender
import java.time.LocalDate
import java.time.ZonedDateTime


data class User0(
  // string UUID
  // e.g. "4f699e2d-a492-40de-a54f-ed05c42203a4"
  val id: String? = null,
  
  // login can be email, phone, nickname#subnickname
  val email: String? = null,
  val emailVerified: Boolean? = null,
  
  // hashed password
  val pwd: String? = null,
  
  val roles: Set<Role> = setOf(),
  
  // must be unique(nickname, subnickname)
  // nickname#subnickname (rrain#cool)
  //subnickname can't contain '#' character
  val nickname: String? = null,
  val subnickname: String? = null,
  
  // e.g. string representation "2023-06-04 15:21:18.094+08"
  // default timezone must be UTC+0
  val created: ZonedDateTime? = null, // UTC+0
  val updated: ZonedDateTime? = null, // UTC+0
  val enabled: Boolean? = null,
  
  val firstName: String? = null, // имя
  val lastName: String? = null, // фамилия
  // e.g. string representation "2023-06-29"
  val birthDate: LocalDate? = null,
  val gender: Gender? = null, // пол: мужской / женский
)
/*
run {
      @Language("sql") val sql = """
        create table if not exists "User" (
          "id" uuid default gen_random_uuid() primary key,
          
          "email" varchar(100) not null unique,
          "pwd" varchar(200) not null,
          
          "nickname" varchar(50) not null,
          "subnickname" varchar(50) not null,
          unique("nickname", "subnickname"),
          
          "created" timestamptz(3) not null,
          "updated" timestamptz(3) not null,
          "enabled" bool default true,
          
          "firstName" varchar(100) not null,
          "lastName" varchar(100) not null,
          "birthDate" date not null
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    
    run {
      // val now = zonedNow()
      // timestamptz '${now.format(timestamptzFormat)}'
      @Language("sql") val sql = """
        insert into "User" (
          "id",
          "email","pwd",
          "nickname","subnickname",
          "created","updated","enabled",
          "firstName","lastName","birthDate"
        ) values (
          uuid '4f699e2d-a492-40de-a54f-ed05c52203a4',
        
          'dedkov_dmitriy_97@mail.ru','${PwdHashing.generateHash("somePwd")}',
          
          'rrain','',
          
          timestamptz '2023-06-02 18:19:07.186+08',
          timestamptz '2023-06-02 18:19:07.186+08',
          true,
          
          'Дмитрий','Дедков',date '1997-11-22'
        )
          on conflict("id") do nothing;
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }


*/
