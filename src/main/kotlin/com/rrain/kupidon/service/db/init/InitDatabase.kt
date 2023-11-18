package com.rrain.kupidon.service.db.init

import com.rrain.kupidon.entity.app.rolePattern
import com.rrain.kupidon.service.db.DatabaseService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.util.extension.get
import com.rrain.kupidon.util.zonedNow
import io.ktor.server.application.*
import reactor.kotlin.core.publisher.toMono



fun Application.initDatabase(){
  val appConfig = environment.config
  val connection = DatabaseService.pool.create().block()!!
  try {
    // create Role table
    run {
      val sql = """
        create table if not exists "Role" (
          "id" uuid default gen_random_uuid() primary key,
          "role" varchar(50) not null unique,
          constraint role_pattern check ("role" ~ '${rolePattern}')
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // fill Role table
    run {
      val sql = """
        insert into "Role" values (
          uuid'1bb41bb5-d396-4cd0-86a4-2e0354eb28b9',
          'ADMIN'
        )
          on conflict("id") do nothing;
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // create User table
    run {
      val sql = """
        create table if not exists "User" (
          "id" uuid default gen_random_uuid() primary key,
          
          "email" varchar(100) not null unique,
          "emailVerified" bool not null default false,
          
          "pwd" varchar(200) not null,
          
          "created" timestamptz(3) not null,
          "updated" timestamptz(3) not null,
          "enabled" bool not null default true,
          
          "name" varchar(100) not null,
          "birthDate" date not null,
          "gender" varchar(50) not null,
          constraint gender_pattern check ("gender" in ('MALE','FEMALE')),
          "aboutMe" varchar(2000) not null default ''
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // fill User table
    run {
      // val now = zonedNow()
      // timestamptz '${now.format(timestamptzFormat)}'
      val sql = """
        insert into "User" (
          "id",
          "email","emailVerified","pwd",
          "created","updated","enabled",
          "name","lastName","birthDate","gender"
        ) values (
          uuid'${appConfig["database.users.admin.id"]}',
        
          '${appConfig["database.users.admin.email"]}',
          true,
          '${appConfig["database.users.admin.pwd"].let(PwdHashing::generateHash)}',
          
          timestamptz'2023-06-02T18:19:07.186+08:00',
          timestamptz'2023-06-02T18:19:07.186+08:00',
          true,
          
          'Дмитрий',date'1997-11-22','MALE'
        )
          on conflict("id") do nothing;
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    run {
      val now = zonedNow()
      // timestamptz '${now.format(timestamptzFormat)}'
      val sql = """
        insert into "User" (
          "id",
          "email","emailVerified","pwd",
          "created","updated","enabled",
          "name","lastName","birthDate","gender"
        ) values (
          uuid'628671b8-aca9-40dd-90db-5a07d6b33025',
        
          'usual_user@test.test',true,'${"usual".let(PwdHashing::generateHash)}',
          
          timestamptz'2023-06-04T15:21:18.094+08:00',
          timestamptz'2023-06-04T15:21:18.094+08:00',
          true,
          
          'Обычный Пользователь',date'2000-06-15','MALE'
        )
          on conflict("id") do nothing;
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // create UserRole table
    run {
      val sql = """
        create table if not exists "UserRole" (
          "userId" uuid not null references "User"("id"),
          "roleId" uuid not null references "Role"("id"),
          primary key ("userId", "roleId")
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // fill UserRole table
    run {
      val sql = """
        insert into "UserRole" values (
          uuid'${appConfig["database.users.admin.id"]}',
          uuid'1bb41bb5-d396-4cd0-86a4-2e0354eb28b9'
        )
          on conflict("userId", "roleId") do nothing;
          
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
  } finally {
    connection.close().toMono().block()
  }
}