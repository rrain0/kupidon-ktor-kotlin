package com.rrain.kupidon.routes

import com.rrain.kupidon.service.db.DatabaseService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking


fun Application.configureRoleRoutes(){
  
  val roleServ = DatabaseService.roleServ
  
  routing {
    
    get("/api/role/exists/{role}") {
      call.respond(object {
        val exists = runBlocking { roleServ.exists(call.parameters["role"]!!) }
      })
    }
    
    get("/api/role/getAll") {
      call.respond(object {
        val roles = runBlocking { roleServ.getAll().toList() }
      })
    }
    
  }
  
}