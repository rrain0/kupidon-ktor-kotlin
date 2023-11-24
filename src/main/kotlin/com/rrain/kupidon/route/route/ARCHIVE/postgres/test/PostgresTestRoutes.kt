package com.rrain.kupidon.route.route.ARCHIVE.postgres.test

import com.rrain.kupidon.service.db.ARCHIVE.postgres.PostgresDbService.transactionDbServ
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



fun Application.configurePostgresTestRoutes(){
  
  
  
  
  
  routing {
    
    data class EmailRequest(val email: String?)
    get("/test/get-all-transactions"){
      val transactions = transactionDbServ.getAll()
      println("transactions: $transactions")
      call.respond(transactions)
    }
    
    
    
    
  }
  
}