package com.rrain.kupidon.routes.routes.http.test

import com.mongodb.MongoException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Field
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.rrain.kupidon.services.mongo.coll
import com.rrain.kupidon.services.mongo.findOneOrInsert
import com.rrain.kupidon.services.mongo.mongoAppDb
import com.rrain.kupidon.services.mongo.mongoDb
import com.rrain.kupidon.services.mongo.useSingleDocTx
import com.rrain.utils.bson.toBJson
import com.rrain.utils.bson.toDoc
import com.rrain.utils.ktor.call.boolQueryParams
import com.rrain.utils.base.`date-time`.toInstant
import com.rrain.utils.base.`delegated-prop`.getIt
import com.rrain.utils.base.uuid.toUuid
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bson.Document
import org.bson.types.ObjectId
import java.util.UUID
import kotlin.random.Random




fun Application.addMongoTestRoutes() {
  
  data class TestItem(
    var id: UUID = UUID.randomUUID(),
    var number: Int = Random.nextInt(),
    var string: String = "",
    var integer: Int = Random.nextInt(),
    var createdAt: Instant = Clock.System.now(),
    var updatedAt: Instant = Clock.System.now(),
  )
  
  val collTestItems by getIt { mongoAppDb.coll<TestItem>("testItems") }
  
  val origMainItem = TestItem(
    "f1ca1e3c-e7d2-4f9b-9419-a099636f58e9".toUuid(),
    7000,
    "ORIGINAL",
    0,
    "2025-06-22T09:39:00.500Z".toInstant(),
    "2025-06-22T09:39:00.500Z".toInstant(),
  )
  val mainItemObjectId = ObjectId("68514f54d72f45c16c63f9c1")
  
  val origSecondItem = TestItem(
    "a1db172a-9138-40fc-a80f-500a1259bcae".toUuid(),
    9999,
    "SECOND",
    -10,
    "2025-06-22T09:45:04.163Z".toInstant(),
    "2025-06-22T09:45:04.163Z".toInstant(),
  )
  val secondItemObjectId = ObjectId("6857bdfa370538ba20e7a102")
  
  // https://kupidon.dev.rraindev:40012/test/mongo/test-items
  val testItemsRoute = "/test/mongo/test-items"
  
  routing {
    
    
    
    
    
    
    get("$testItemsRoute/reset/by-id") {
      val mainItem = collTestItems
        .findOneAndUpdate(
          Filters.eq(TestItem::id.name, origMainItem.id),
          $$"{ $set: $${origMainItem.toBJson()} }".toDoc(),
          FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
        )
      val secondItem = collTestItems
        .findOneAndUpdate(
          Filters.eq(TestItem::id.name, origSecondItem.id),
          $$"{ $set: $${origSecondItem.toBJson()} }".toDoc(),
          FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
        )
      call.respond(mapOf(
        "mainItem" to mainItem,
        "secondItem" to secondItem,
        ))
    }
    
    get("$testItemsRoute/add/random") {
      var item = TestItem()
      item = collTestItems
        .findOneOrInsert(
          Filters.eq(TestItem::id.name, item.id),
          item,
        )
      call.respond(mapOf("item" to item))
    }
    
    // Падает с ошибкой duplicate key.
    // Драйвер не пытается снова с рандомным ObjectId.
    // Правда не понятно, будет ли драйвер ретраить, если мы не передадим ничего
    // и он сам сгенерирует дублирующееся значение.
    get("$testItemsRoute/add/duplicate-object-id") {
      val duplicateObjectId = mainItemObjectId
      mongoAppDb.coll<Map<String, Any?>>("testItems").insertOne(mapOf(
        "_id" to duplicateObjectId,
        "id" to UUID.randomUUID(),
        "number" to Random.nextInt(),
      ))
      call.respond(HttpStatusCode.OK)
    }
    
    get("$testItemsRoute/add/duplicate-id") {
      try {
        val duplicateId = origMainItem.id
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
    
    get("$testItemsRoute/add/duplicate-id-then-loop-random-id") {
      val duplicateId = origMainItem.id
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
    
    get("$testItemsRoute/add/if-not-exists-by-id-and-number") {
      val duplicateId = origMainItem.id
      val duplicateNumber = origMainItem.number
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
            $$"{ $setOnInsert: $${item.toBJson()} }".toDoc(),
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
    
    
    
    get("$testItemsRoute/find-one/by-string-original") {
      val duplicateString = origMainItem.string
      
      val item = collTestItems
        .find(Filters.eq(TestItem::string.name, duplicateString))
        .firstOrNull()
      
      call.respond(mapOf("item" to item))
    }
    
    get("$testItemsRoute/find-one-and-update/by-string-original") {
      val duplicateString = origMainItem.string
      
      val item = collTestItems
        // .withReadPreference(ReadPreference.primary())
        // .withReadConcern(ReadConcern.LINEARIZABLE)
        // .withWriteConcern(WriteConcern.MAJORITY.withJournal(true))
        .findOneAndUpdate(
          Filters.eq(TestItem::string.name, duplicateString),
          //Updates.set(TestItem::integer.name, 1),
          Updates.set(TestItem::string.name, duplicateString + "2"),
          FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
        )
      
      call.respond(mapOf("item" to item))
    }
    
    get("$testItemsRoute/find-one-and-update/main-item/by-id") {
      val item = collTestItems
        // .withReadPreference(ReadPreference.primary())
        // .withReadConcern(ReadConcern.LINEARIZABLE)
        // .withWriteConcern(WriteConcern.MAJORITY.withJournal(true))
        .findOneAndUpdate(
          Filters.eq(TestItem::id.name, origMainItem.id),
          Updates.set(TestItem::integer.name, 1),
          //Updates.set(TestItem::string.name, duplicateString + "2"),
          FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
        )
      
      call.respond(mapOf("item" to item))
    }
    
    get("$testItemsRoute/find-one-and-update/second-item/by-id") {
      val item = collTestItems
        .findOneAndUpdate(
          Filters.eq(TestItem::id.name, origSecondItem.id),
          Updates.set(TestItem::integer.name, 1),
          //Updates.set(TestItem::string.name, duplicateString + "2"),
          FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
        )
      
      call.respond(mapOf("item" to item))
    }
    
    get("$testItemsRoute/find-one-and-update/main-item/transaction/wait/by-string-original") {
      val duplicateString = origMainItem.string
      val item = useSingleDocTx { session, abort ->
        val item = collTestItems
          .findOneAndUpdate(
            session,
            Filters.eq(TestItem::string.name, duplicateString),
            //Updates.set(TestItem::integer.name, 1),
            //Updates.set(TestItem::string.name, duplicateString),
            Updates.set(TestItem::string.name, duplicateString + "1"),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
          )
        delay(6000)
        abort()
        item
      }
      call.respond(mapOf("item" to item))
    }
    
    get("$testItemsRoute/find-one/main-item/transaction/by-string-original") {
      val duplicateString = origMainItem.string
      val item = useSingleDocTx { session, abort ->
        val item =
          collTestItems
            // .withReadPreference(ReadPreference.primary())
            // .withReadConcern(ReadConcern.LINEARIZABLE)
            // .withWriteConcern(WriteConcern.MAJORITY.withJournal(true))
            .find(
              session,
              Filters.eq(TestItem::string.name, duplicateString),
            )
            .firstOrNull()
        item
      }
      call.respond(mapOf("item" to item))
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
      useSingleDocTx { session ->
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
      
      
      useSingleDocTx { session ->
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