package com.rrain.kupidon.route.route.test

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.useTransaction
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.util.UUID




fun Application.configureMongoTestRoutes(){
  
  
  
  
  routing {
    
    
    
    
    
    data class Test2(
      val _id: ObjectId,
      val id: UUID,
      val string: String,
      val map: Document?,
    )
    get("/test/mongo/test/test2/get-all"){
      val coll = MongoDbService.db("test").coll<Test2>("test2")
      call.respond(coll.find().toList())
    }
    
    
    data class Movie(
      val _id: ObjectId,
      val title: String,
      val genres: List<String>,
      val runtime: Int,
      val rated: String,
      val year: Int,
      val directors: List<String>,
      val cast: List<String>,
      val type: String,
    )
    get("/test/mongo/get-all"){
      val testDb = MongoDbService.db("test")
      
      //testDb.createView()
      
      val movieCollection = testDb.coll<Movie>("movies")
      val movies = movieCollection.find()
      val moviesList = movies.toList()
      println("moviesList[0]._id.toHexString() ${moviesList[0]._id.toHexString()}")
      println("movies: $moviesList")
      call.respond(moviesList)
    }
    
    
    get("/test/mongo/update-all"){
      val movieColl = MongoDbService.db("test").coll<Movie>("movies")
      movieColl.updateMany(
        Filters.empty(),
        Updates.set(Movie::title.name, "film2")
      )
      val movies = movieColl.find().toList()
      call.respond(movies)
    }
    
    
    get("/test/mongo/get-all-transaction"){
      MongoDbService.client.useTransaction {
        val movieColl = MongoDbService.db("test").coll<Movie>("movies")
        
        
        movieColl.updateMany(
          it,
          Filters.empty(),
          Updates.set(Movie::title.name, "film")
        )
        
        delay(5000)
        
        movieColl.updateMany(
          it,
          Filters.empty(),
          Updates.set(Movie::title.name, "movie")
        )
        
        delay(5000)
        
        
        val movies = movieColl.find(it).toList()
        call.respond(movies)
      }
    }
    
  }
  
}