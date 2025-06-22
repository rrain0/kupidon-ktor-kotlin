package com.rrain.kupidon.route.routes.test

import com.mongodb.MongoException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.rrain.kupidon.service.mongo.coll
import com.rrain.kupidon.service.mongo.fullUpdatesSet
import com.rrain.kupidon.service.mongo.mongoAppDb
import com.rrain.kupidon.service.mongo.mongoDb
import com.rrain.kupidon.service.mongo.useSingleDocTransaction
import com.rrain.`util-ktor`.call.boolQueryParams
import com.rrain.util.any.objectPrimaryPropsToMap
import com.rrain.util.uuid.toUuid
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.util.UUID
import kotlin.random.Random




data class TestItem(
  var id: UUID = UUID.randomUUID(),
  var number: Int = Random.nextInt(),
  var string: String = "",
  var integer: Int = Random.nextInt(),
)
val collTestItems get() = mongoAppDb.coll<TestItem>("testItems")



fun Application.addMongoTestRoutes() {
  routing {
    
    get("/test/mongo/test-items/reset/main-test-item/by-id") {
      val item = TestItem(
        "f1ca1e3c-e7d2-4f9b-9419-a099636f58e9".toUuid(),
        7000,
        "ORIGINAL",
        0
      )
      collTestItems.
      updateOne(
        Filters.eq(TestItem::id.name, item.id),
        Updates.combine(item.fullUpdatesSet())
      )
      call.respond("OK")
    }
    
    get("/test/mongo/test-items/add/random") {
      collTestItems.insertOne(TestItem())
      call.respond(HttpStatusCode.OK)
    }
    
    // Падает с ошибкой duplicate key.
    // Драйвер не пытается снова с рандомным ObjectId.
    // Правда не понятно, будет ли драйвер ретраить, если мы не передадим ничего
    // и он сам сгенерирует дублирующееся значение.
    get("/test/mongo/test-items/add/duplicate-object-id") {
      val duplicateObjectId = ObjectId("68542d7078b388190d4150fa")
      mongoAppDb.coll<Map<String, Any?>>("testItems").insertOne(mapOf(
        "_id" to duplicateObjectId,
        "id" to UUID.randomUUID(),
        "number" to Random.nextInt(),
      ))
      call.respond(HttpStatusCode.OK)
    }
    
    get("/test/mongo/test-items/add/duplicate-id") {
      try {
        val duplicateId = "f1ca1e3c-e7d2-4f9b-9419-a099636f58e9".toUuid()
        collTestItems.insertOne(TestItem(duplicateId))
        call.respond(HttpStatusCode.OK)
      }
      catch (ex: MongoWriteException) {
        if (ex.code == 11000) {
          call.respond(HttpStatusCode.InternalServerError, mapOf(
            "message" to "Unique index violation",
            "cause" to ex.toString(),
          ))
        }
      }
    }
    
    get("/test/mongo/test-items/add/duplicate-id-then-loop-random-id") {
      val duplicateId = "f1ca1e3c-e7d2-4f9b-9419-a099636f58e9".toUuid()
      var newId = duplicateId
      for (i in 100 downTo 0) {
        try {
          println("newId: $newId")
          collTestItems.insertOne(TestItem(newId))
          break
        }
        catch (ex: MongoWriteException) {
          if (i == 0) throw ex
          if (ex.code == 11000) {
            newId = UUID.randomUUID()
          }
          else throw ex
        }
      }
      call.respond(HttpStatusCode.OK)
    }
    
    get("/test/mongo/test-items/add/if-not-exists-by-id-and-number") {
      val duplicateId = "f1ca1e3c-e7d2-4f9b-9419-a099636f58e9".toUuid()
      val duplicateNumber = 7000
      var item = TestItem(
        if (call.boolQueryParams["dupId"]) duplicateId else UUID.randomUUID(),
        if (call.boolQueryParams["dupNumber"]) duplicateNumber else Random.nextInt(),
        "some string",
      )
      
      // Use 'mongoUniqueViolationRetry' & 'findOneOrInsert' to avoid code duplication
      val attemptsCnt = 100
      for (i in 1..attemptsCnt) {
        try {
          println("i: $i, item: $item")
          item = collTestItems.findOneAndUpdate(
            Filters.eq(TestItem::number.name, item.number),
            Updates.setOnInsert(Document(item.objectPrimaryPropsToMap())),
            FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER),
          )!!
          break
        }
        catch (ex: MongoException) {
          if (i == attemptsCnt) throw ex
          // Ошибка нарушения уникальности индекса
          else if (ex.code == 11000) {
            item.id = UUID.randomUUID()
          }
          else throw ex
        }
      }
      
      println("item: $item")
      call.respond(mapOf("item" to item))
    }
    
    
    
    get("/test/mongo/test-items/get/by-string-original") {
      val duplicateString = "ORIGINAL"
      
      var item = collTestItems
        .find(Filters.eq(TestItem::string.name, duplicateString))
        .firstOrNull()
      
      call.respond(mapOf("item" to item))
    }
    
    get("/test/mongo/test-items/update/by-string-original") {
      val duplicateString = "ORIGINAL"
      
      collTestItems
        .updateOne(
          Filters.eq(TestItem::string.name, duplicateString),
          Updates.combine(
            Updates.set(TestItem::string.name, duplicateString + "1"),
            Updates.inc(TestItem::integer.name, 1),
          )
        )
      
      call.respond("OK")
    }
    
    get("/test/mongo/test-items/transaction/find-and-update/wait/by-string-original") {
      val duplicateString = "ORIGINAL"
      
      useSingleDocTransaction { session, abort ->
        var item = collTestItems
          .findOneAndUpdate(
            session,
            Filters.eq(TestItem::string.name, duplicateString),
            // Попытка установить то же самое значение полю всё равно вызывает блокировку для других
            Updates.set(TestItem::string.name, duplicateString + "2"),
            //Updates.inc(TestItem::integer.name, 1),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
          )
        delay(6000)
        //abort()
      }
      call.respond("OK")
    }
    
    get("/test/mongo/test-items/transaction/find/by-string-original") {
      val duplicateString = "ORIGINAL"
      
      useSingleDocTransaction { session, abort ->
        var item =
          collTestItems.find(
            session,
            Filters.eq(TestItem::string.name, duplicateString),
          )
          .firstOrNull()
        abort()
      }
      call.respond("OK")
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
    get("/test/mongo/get-all") {
      val testDb = mongoDb("test")
      
      //testDb.createView()
      
      val movieCollection = testDb.coll<Movie>("movies")
      val movies = movieCollection.find()
      val moviesList = movies.toList()
      println("moviesList[0]._id.toHexString() ${moviesList[0]._id.toHexString()}")
      println("movies: $moviesList")
      call.respond(moviesList)
    }
    
    
    get("/test/mongo/update-all") {
      val movieColl = mongoDb("test").coll<Movie>("movies")
      movieColl.updateMany(
        Filters.empty(),
        Updates.set(Movie::title.name, "film2")
      )
      val movies = movieColl.find().toList()
      call.respond(movies)
    }
    
    
    get("/test/mongo/get-all-transaction") {
      useSingleDocTransaction { session ->
        val movieColl = mongoDb("test").coll<Movie>("movies")
        
        
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
    
    
    get("/test/mongo/indexes") {
      data class Index(
        val _id: ObjectId,
        val index: Int,
      )
      val n_Id = Index::_id.name
      val nIndex = Index::index.name
      
      
      useSingleDocTransaction { session ->
        val coll = mongoDb("test").coll<Index>("indexes")
        
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