package com.rrain.kupidon.archive

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.util.get
import org.jetbrains.exposed.sql.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun Application.configureDatabases() {
  
  val appConfig = environment.config
  
  val db = Database.connect(
    /*url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    user = "root",
    driver = "org.h2.Driver",
    password = ""*/
    url = appConfig["database.url"],
    user = appConfig["database.user"],
    password = appConfig["database.pwd"],
  )
  
  
  val roleServ = RoleDbService(db)
  runBlocking(Dispatchers.IO) {
    Role.values().forEach { roleServ.createIgnore(it) }
  }
  
  val userServ = UserDbService(db)
  
  val userRoleServ = UserRoleDbService(db)
  
  
  routing {
    post("/api/db/role/getAll"){
      val allRoles = roleServ.getAll()
      call.respond(object {
        val allRoles = allRoles
      })
    }
  }
  
  val userService = UserService(db)
  routing {
    // Create user
    post("/users") {
      val user = call.receive<User>()
      val id = userService.create(user)
      call.respond(HttpStatusCode.Created, id)
    }
    // Read user
    get("/users/{id}") {
      val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
      val user = userService.read(id)
      if (user != null) {
        call.respond(HttpStatusCode.OK, user)
      } else {
        call.respond(HttpStatusCode.NotFound)
      }
    }
    // Update user
    put("/users/{id}") {
      val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
      val user = call.receive<User>()
      userService.update(id, user)
      call.respond(HttpStatusCode.OK)
    }
    // Delete user
    delete("/users/{id}") {
      val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
      userService.delete(id)
      call.respond(HttpStatusCode.OK)
    }
  }
}
