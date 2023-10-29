package com.rrain.kupidon.service.db.init

import com.rrain.kupidon.entity.app.rolePattern
import com.rrain.kupidon.service.DatabaseService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.util.get
import com.rrain.kupidon.util.zonedNow
import io.ktor.server.application.*
import org.intellij.lang.annotations.Language
import reactor.kotlin.core.publisher.toMono



fun Application.initDatabase(){
  val appConfig = environment.config
  val connection = DatabaseService.pool.create().block()!!
  try {
    // create Role table
    run {
      @Language("sql") val sql = """
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
      @Language("sql") val sql = """
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
      @Language("sql") val sql = """
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
          "sex" varchar(50) not null,
          constraint sex_pattern check ("sex" in ('MALE','FEMALE'))
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
          "email","emailVerified","pwd",
          "created","updated","enabled",
          "name","lastName","birthDate","sex"
        ) values (
          uuid'${appConfig["database.users.admin.id"]}',
        
          '${appConfig["database.users.admin.email"]}',
          true,
          '${PwdHashing.generateHash(appConfig["database.users.admin.pwd"])}',
          
          timestamptz'2023-06-02 18:19:07.186+0800',
          timestamptz'2023-06-02 18:19:07.186+0800',
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
      @Language("sql") val sql = """
        insert into "User" (
          "id",
          "email","emailVerified","pwd",
          "created","updated","enabled",
          "name","lastName","birthDate","sex"
        ) values (
          uuid'628671b8-aca9-40dd-90db-5a07d6b33025',
        
          'usual_user@test.test',true,'${PwdHashing.generateHash("usual")}',
          
          timestamptz'2023-06-04 15:21:18.094+0800',
          timestamptz'2023-06-04 15:21:18.094+0800',
          true,
          
          'Обычный Пользователь',date'2000-06-15','MALE'
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
          "roleId" uuid not null references "Role"("id"),
          primary key ("userId", "roleId")
        );
      """.trimIndent()
      connection.createStatement(sql).execute().toMono().block()
    }
    
    // fill UserRole table
    run {
      @Language("sql") val sql = """
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