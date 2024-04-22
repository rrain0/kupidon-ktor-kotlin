package com.rrain.kupidon.service.db.mongo

import com.mongodb.*
import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.rrain.kupidon.util.get
import com.rrain.kupidon.util.toTimestamp
import com.rrain.kupidon.util.toZonedDateTime
import io.ktor.server.application.*
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.UuidRepresentation
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import java.time.ZonedDateTime




fun Application.configureMongoDbService(){
  
  val appConfig = environment.config
  
  val appName = appConfig["db.connection.application-name"]
  val host = appConfig["db.connection.mongo.host"]
  val port = appConfig["db.connection.mongo.port"].toInt()
  val rs = appConfig["db.connection.mongo.rs"]
  val database = appConfig["db.connection.mongo.database"]
  val user = appConfig["db.connection.mongo.user"]
  val pwd = appConfig["db.connection.mongo.pwd"]
  
  
  
  val codecRegistry = CodecRegistries.fromRegistries(
    //CodecRegistries.fromCodecs(IntegerCodec(), PowerStatusCodec()),
    //CodecRegistries.fromProviders(MonolightCodecProvider()),
    
    CodecRegistries.fromCodecs(
      object : Codec<ZonedDateTime> {
        override fun getEncoderClass() = ZonedDateTime::class.java
        override fun encode(writer: BsonWriter, value: ZonedDateTime, encoderContext: EncoderContext) {
          writer.writeDateTime(value.toTimestamp())
        }
        override fun decode(reader: BsonReader, decoderContext: DecoderContext): ZonedDateTime {
          return reader.readDateTime().toZonedDateTime()
        }
      }
    ),
    
    MongoClientSettings.getDefaultCodecRegistry()
  )
  
  
  // https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/connection/connection-options/
  val connSettings = MongoClientSettings.builder()
    .applicationName(appName)
    .also {
      if (user.isNotEmpty() && pwd.isNotEmpty())
        it.credential(MongoCredential.createCredential(user, database, pwd.toCharArray()))
    }
    .applyToClusterSettings {
      it.requiredReplicaSetName(rs)
      it.hosts(listOf(ServerAddress(host,port)))
    }
    .applyToConnectionPoolSettings {
      // min connections pool size
      //it.minSize(0)
      // max connections pool size
      //it.maxSize(100)
    }
    .applyToLoggerSettings {  }
    .applyToServerSettings {  }
    .applyToSocketSettings {  }
    .applyToSslSettings {  }
    .uuidRepresentation(UuidRepresentation.STANDARD)
    .codecRegistry(codecRegistry)
    .build()
  
  // Reuse Your Client
  // As each MongoClient represents a thread-safe pool of connections to the database,
  // most applications only require a single instance of a MongoClient,
  // even across multiple threads.
  val mongoClient = MongoClient.create(connSettings)
  MongoDbService.client = mongoClient
  
  val databaseName = appConfig["db.connection.mongo.database"]
  MongoDbService.dbName = databaseName
  
}



object MongoDbService {
  lateinit var client: MongoClient
  lateinit var dbName: String
  val db get() = client.db(dbName)
  fun db(dbName: String) = client.db(dbName)
}




val MongoClient.db get() = this.getDatabase(MongoDbService.dbName)
fun MongoClient.db(dbName: String) = this.getDatabase(dbName)
inline fun <reified T : Any> MongoDatabase.coll(collName: String)
  = this.getCollection<T>(collName)




suspend inline fun <T> MongoClient.useTransaction
(block: (session: ClientSession)->T): T {
  val session = this.startSession()
  
  val transactionOpts = TransactionOptions.builder()
    // Read preference describes how MongoDB clients route read operations
    // to the members of a replica set.
    // https://www.mongodb.com/docs/manual/core/read-preference/#std-label-replica-set-read-preference
    .readPreference(ReadPreference.primary())
    // The readConcern option allows you to control the consistency
    // and isolation properties of the data read from replica sets and replica set shards.
    // https://www.mongodb.com/docs/manual/reference/read-concern/
    .readConcern(ReadConcern.LOCAL)
    // Write concern describes the level of acknowledgment requested from MongoDB for write operations
    // to a standalone mongod or to Replica sets or to sharded clusters.
    // In sharded clusters, mongos instances will pass the write concern on to the shards.
    // https://www.mongodb.com/docs/manual/reference/write-concern/
    .writeConcern(WriteConcern.MAJORITY)
    .build()
  
  return session.use {
    it.startTransaction(transactionOpts)
    val result = block(session)
    it.commitTransaction()
    result
  }
}
