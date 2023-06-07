package com.rrain.kupidon.service.db.init

import com.rrain.kupidon.service.DatabaseService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.util.timestamptzFormat
import com.rrain.kupidon.util.zonedNow
import io.ktor.server.application.*
import org.intellij.lang.annotations.Language
import reactor.kotlin.core.publisher.toMono


fun Application.initDatabase(){
  val connection = DatabaseService.pool.create().block()!!
  try {
    // create Role table
    run {
      @Language("sql") val sql = """
        create table if not exists "Role" (
          "role" varchar(50) not null primary key,
          constraint role_pattern check ("role" ~ '^[a-zA-Z][a-zA-Z_\-\d]*$')
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // fill Role table
    run {
      @Language("sql") val sql = """
        insert into "Role" values ('ADMIN')
          on conflict("role") do nothing;
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // TODO do i need to create separate connection for each query?
    
    // create User table
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
    
    // fill User table
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
          uuid '4f699e2d-a492-40de-a54f-ed05c42203a4',
        
          'dedkov_dmitriy_97@mail.ru','${PwdHashing.generateHash("workin123")}',
          
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
    run {
      val now = zonedNow()
      // timestamptz '${now.format(timestamptzFormat)}'
      @Language("sql") val sql = """
        insert into "User" (
          "id",
          "email","pwd",
          "nickname","subnickname",
          "created","updated","enabled",
          "firstName","lastName","birthDate"
        ) values (
          uuid '628671b8-aca9-40dd-90db-5a07d6b33025',
        
          'usual_user@test.test','${PwdHashing.generateHash("usual")}',
          
          'usual','user',
          
          timestamptz '2023-06-04 15:21:18.094+08',
          timestamptz '2023-06-04 15:21:18.094+08',
          true,
          
          'Обычный','Пользователь',date '2000-06-15'
        )
          on conflict("id") do nothing;
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // create UserRole table
    run {
      @Language("sql") val sql = """
        create table if not exists "UserRole" (
          "userId" uuid not null references "User"("id"),
          "role" varchar(50) not null references "Role"("role"),
          primary key ("userId", "role")
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // fill UserRole table
    run {
      @Language("sql") val sql = """
        
        insert into "UserRole" values (
          (select "id" from "User"
            where "id" = uuid '4f699e2d-a492-40de-a54f-ed05c42203a4'
            limit 1
          ),
          'ADMIN'
        )
          on conflict("userId", "role") do nothing;
          
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
  } finally {
    connection.close().toMono().block()
  }
}