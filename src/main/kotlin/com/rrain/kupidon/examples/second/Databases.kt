package com.rrain.kupidon.examples.second

import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.examples.second.RoleDbService
import com.rrain.kupidon.examples.second.UserDbService
import com.rrain.kupidon.util.get
import com.rrain.kupidon.util.timestamptzFormat
import com.rrain.kupidon.util.zonedNow
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.configureDatabases() {
  
  val appConfig = environment.config
  
  val db = Database.connect(
    url = appConfig["database.connection.url"],
    user = appConfig["database.connection.user"],
    password = appConfig["database.connection.pwd"],
  )
  
  val roleServ = RoleDbService(db)
  
  // create Role table
  transaction(db) {
    exec("""
      create table if not exists "Role" (
        "role" varchar(50) not null primary key
      );
    """.trimIndent())
  }
  
  // fill Role table
  transaction(db) {
    exec("""
      insert into "Role" values ('ADMIN')
        on conflict("role") do nothing;
      insert into "Role" values ('USER')
        on conflict("role") do nothing;
      insert into "Role" values ('WANDERER')
        on conflict("role") do nothing;
    """.trimIndent())
  }
  
  val userServ = UserDbService(db)
  
  // create User table
  transaction(db) {
    exec("""
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
    """.trimIndent())
  }
  
  // fill User table
  val now = zonedNow()
  transaction(db) {
    exec("""
      insert into "User" (
        "id",
      
        "email",
        "pwd",
        
        "nickname",
        "subnickname",
        
        "created",
        "updated",
        "enabled",
        
        "firstName",
        "lastName",
        "birthDate"
      ) values (
        uuid '4f699e2d-a492-40de-a54f-ed05c42203a4',
      
        'dedkov_dmitriy_97@mail.ru',
        '${PwdHashing.generateHash("workin123")}',
        
        'rrain',
        '',
        
        timestamptz '${now.format(timestamptzFormat)}',
        timestamptz '${now.format(timestamptzFormat)}',
        true,
        
        'Дмитрий',
        'Дедков',
        date '1997-11-22'
      )
        on conflict("email") do nothing;
    """.trimIndent())
  }
  
  // create UserRole table
  transaction(db) {
    exec("""
      create table if not exists "UserRole" (
        "userId" uuid not null references "User"("id"),
        "role" varchar(50) not null references "Role"("role"),
        primary key ("userId", "role")
      );
    """.trimIndent())
  }
  
  // fill UserRole table
  transaction(db) {
    exec("""
      insert into "UserRole" values (
        (select "id" from "User"
          where "email"='dedkov_dmitriy_97@mail.ru'
          limit 1
        ),
        'ADMIN'
      )
        on conflict("userId", "role") do nothing;
    """.trimIndent())
  }
  
  
  routing {
    
    get("/api/resource/role/exists/{role}") {
      call.respond(mapOf("exists" to roleServ.exists(call.parameters["role"]!!)))
    }
    
    get("/api/resource/role/getAll") {
      call.respond(roleServ.getAll())
    }
    
    get("/api/resource/user/getById/{id}") {
      call.respond( object {
        val user = userServ.getById(call.parameters["id"]!!)
      } )
    }
    
  }
  
  
}
