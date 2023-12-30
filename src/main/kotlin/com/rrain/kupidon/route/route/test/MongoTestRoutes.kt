package com.rrain.kupidon.route.route.test

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates
import com.mongodb.client.model.WriteModel
import com.mongodb.internal.bulk.WriteRequest
import com.mongodb.internal.bulk.WriteRequestWithIndex
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
import javax.print.Doc


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
      MongoDbService.client.useTransaction { session ->
        val movieColl = MongoDbService.db("test").coll<Movie>("movies")
        
        
        movieColl.updateMany(
          session,
          Filters.empty(),
          Updates.set(Movie::title.name, "film")
        )
        
        delay(5000)
        
        movieColl.updateMany(
          session,
          Filters.empty(),
          Updates.set(Movie::title.name, "movie")
        )
        
        delay(5000)
        
        
        val movies = movieColl.find(session).toList()
        call.respond(movies)
      }
    }
    
    
    get("/test/mongo/indexes"){
      data class Index(
        val _id: ObjectId,
        val index: Int,
      )
      val n_Id = Index::_id.name
      val nIndex = Index::index.name
      
      
      MongoDbService.client.useTransaction { session ->
        val coll = MongoDbService.db("test").coll<Index>("indexes")
        
        val id1 = ObjectId("658f92c9fabdb93de50b6c36")
        val id2 = ObjectId("658f92c9fabdb93de50b6c37")
        coll.aggregate(session, listOf(
          Aggregates.match(Document(n_Id, id1)),
          Aggregates.limit(1),
          Aggregates.set(Field(nIndex, 1)),
          Aggregates.match(Document(n_Id, id2)),
          Aggregates.limit(1),
          Aggregates.set(Field(nIndex, 0)),
        ))
        
        
        /*
        coll.bulkWrite(session,listOf(
          UpdateOneModel<Index>(
            Filters.eq(n_Id, ObjectId("658f92c9fabdb93de50b6c36")),
            Updates.set(nIndex, 1)
          ),
          UpdateOneModel<Index>(
            Filters.eq(n_Id, ObjectId("658f92c9fabdb93de50b6c37")),
            Updates.set(nIndex, 0)
          ),
        ))
        */
        
        /*coll.updateOne(session,
          Filters.eq(n_Id, ObjectId("658f92c9fabdb93de50b6c36")),
          Updates.set(nIndex, 1)
        )*/
        
        //delay(5000)
        
        /*coll.updateOne(session,
          Filters.eq(n_Id, ObjectId("658f92c9fabdb93de50b6c37")),
          Updates.set(nIndex, 0)
        )*/
        
        //delay(5000)
        
        val documents = coll.find(session).toList()
        call.respond(documents)
      }
    }
    
  }
  
}