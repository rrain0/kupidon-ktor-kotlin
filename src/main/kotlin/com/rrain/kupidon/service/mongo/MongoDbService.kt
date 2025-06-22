package com.rrain.kupidon.service.mongo

import com.mongodb.*
import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.rrain.kupidon.service.mongo.model.ChatMessageMongo
import com.rrain.kupidon.service.mongo.model.ChatMongo
import com.rrain.kupidon.service.mongo.model.UserMongo
import com.rrain.kupidon.service.mongo.model.UserToUserLikeMongo
import com.rrain.`util-ktor`.application.appConfig
import com.rrain.`util-ktor`.application.get
import com.rrain.util.`date-time`.toTimestamp
import com.rrain.util.`date-time`.toZonedDateTime
import io.ktor.http.*
import io.ktor.server.application.*
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.UuidRepresentation
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistries
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.ZonedDateTime
import javax.net.ssl.*




fun Application.configureMongoDbService() {
  
  val appName = appConfig["db.connection.application-name"]
  val host = appConfig["db.connection.mongo.host"]
  val port = appConfig["db.connection.mongo.port"].toInt()
  val rs = appConfig["db.connection.mongo.rs"]
  val database = appConfig["db.connection.mongo.database"]
  val backendClientCert = appConfig["db.connection.mongo.backendClientCert"]
  val caCert = appConfig["db.connection.mongo.caCert"]
  
  
  val connectionString = URLBuilder(
    $$"mongodb://$$host:$$port/" +
      $$"?replicaSet=$$rs&tls=true&authSource=$external&authMechanism=MONGODB-X509"
  ).buildString()
  
  
  // https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/connection/connection-options/
  val connectionSettings = MongoClientSettings.builder()
    .applicationName(appName)
    .applyConnectionString(ConnectionString(connectionString))
    .applyToSslSettings { builder ->
      builder.enabled(true)
      builder.context(getMongoSslContext(backendClientCert, caCert))
    }
    .uuidRepresentation(UuidRepresentation.STANDARD)
    .codecRegistry(getMongoCodecRegisty())
    .build()
  
  // Reuse Your Client
  // As each MongoClient represents a thread-safe pool of connections to the database,
  // most applications only require a single instance of a MongoClient,
  // even across multiple threads.
  val mongoClient = MongoClient.create(connectionSettings)
  
  MongoDbService.config = MongoDbService.Config(
    client = mongoClient,
    dbName = database,
  )
  
}



fun getMongoCodecRegisty() = CodecRegistries.fromRegistries(
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
    },
  ),
  
  MongoClientSettings.getDefaultCodecRegistry(),
)



fun getMongoSslContext(backendClientCert: String, caCert: String) = run {
  val pkcs12CertPwd = "0000".toCharArray()
  val keyStore = KeyStore.getInstance("PKCS12").apply {
    FileInputStream(backendClientCert).use {
      load(it, pkcs12CertPwd)
    }
  }
  val keyManagerFactory = KeyManagerFactory
    .getInstance(KeyManagerFactory.getDefaultAlgorithm())
    .apply {
      init(keyStore, pkcs12CertPwd)
    }
  
  
  
  val caCert = CertificateFactory.getInstance("X.509")
    .generateCertificate(FileInputStream(File(caCert))) as X509Certificate
  
  // Create a custom TrustManager that trusts this certificate
  val trustManagers = arrayOf<TrustManager>(object: X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOf(caCert)
    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) { }
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String?) {
      // Optionally validate the server certificate here
      if (certs.any { it == caCert }) return
      throw SecurityException("Server certificate not trusted")
    }
  })
  
  
  
  // Initialize SSLContext
  val sslContext = SSLContext.getInstance("TLS").apply {
    init(keyManagerFactory.keyManagers, trustManagers, SecureRandom())
  }
  
  sslContext
}




object MongoDbService {
  
  data class Config(
    val client: MongoClient,
    val dbName: String,
  )
  
  lateinit var config: Config
  
  val client get() = config.client
  val appDbName get() = config.dbName
}

val mongo get() = MongoDbService.client

fun MongoClient.db(dbName: String) = this.getDatabase(dbName)
val MongoClient.appDb get() = this.getDatabase(MongoDbService.appDbName)

inline fun <reified T : Any> MongoDatabase.coll(collName : String) = (
  this.getCollection<T>(collName)
)


fun mongoDb(dbName: String) = mongo.db(dbName)
val mongoAppDb get() = mongo.appDb

object CollNames {
  val users = "users"
  val userToUserLikes = "userToUserLikes"
  val chats = "chats"
  val chatMessages = "chatsMessages"
}

val collUsers get() = mongoAppDb.coll<UserMongo>(CollNames.users)
val collUserToUserLikes get() = mongoAppDb.coll<UserToUserLikeMongo>(CollNames.userToUserLikes)
val collChats get() = mongoAppDb.coll<ChatMongo>(CollNames.chats)
val collChatsMessages get() = mongoAppDb.coll<ChatMessageMongo>(CollNames.chatMessages)





// READ PREFERENCE
// https://www.mongodb.com/docs/manual/core/read-preference/#std-label-replica-set-read-preference
// Read preference describes how MongoDB clients route read operations to the members of a replica set.
// Useful for balancing consistency vs. latency.
// `primary`: Reads only from the primary node (default, ensures strong consistency).
// `primaryPreferred`: Prefers the primary but falls back to a secondary if the primary is unavailable.
// `secondary`: Reads from secondary nodes (may return slightly stale data but reduces load on the primary).
// `secondaryPreferred`: Prefers secondaries but falls back to the primary if no secondary is available.
// `nearest`: Reads from the node with the lowest latency, regardless of primary or secondary.

// READ CONCERN
// https://www.mongodb.com/docs/manual/reference/read-concern/
// The readConcern option allows you to control the consistency
// and isolation properties of the data read from replica sets and replica set shards.
// Ensures the data read is consistent with the transaction’s requirements.
// `local`: Sufficient for SINGLE-DOCUMENT operations.
// Returns the most recent data available on the node (default, may not reflect writes acknowledged by a majority).
// `majority`: Returns data that has been acknowledged by a majority of replica set members (ensures durability but may be slower).
// `linearizable`: Ensures reads reflect all successful writes that completed before the read (strong consistency, only for primary reads).
// `snapshot`: Reads from a consistent snapshot of data at the transaction’s start (ideal for multi-document transactions, MongoDB 4.0+).
// `available`: Reads the most recent data, even if uncommitted (not typically used in transactions).

// WRITE CONCERN
// https://www.mongodb.com/docs/manual/reference/write-concern/
// Write concern describes the level of acknowledgment requested from MongoDB for write operations
// to a standalone mongod or to Replica sets or to sharded clusters.
// In sharded clusters, mongos instances will pass the write concern on to the shards.
// Ensures writes are durable and consistent across the replica set.
// `w: 1`: Acknowledges the write on the primary node (default, fast but less durable).
// `w: "majority"`: For CRITICAL transactions. Acknowledges the write after a majority of replica set members replicate it (slower but durable).
// `w: <number>`: Acknowledges after `<number>` nodes replicate the write.
// `journal: true`: Ensures writes are written to the journal on disk before acknowledgment (increases durability).
// `wtimeout: <ms>`: Sets a timeout (in milliseconds) for the write operation to complete, or it fails.

// NOTES
// Попытка установить то же самое значение полю всё равно вызывает блокировку для других.

// Как использовать:
// Берём 1 документ через findOneAndUpdate, обновляем updateAt чтобы залочилось.
// Смотрим документ, производим изменения, делаем updateOne.
// Если кто-то другой попытается записать в документ, он будет ждать окончания транзакции.
suspend inline fun <T> useSingleDocTransaction(
  block: (session: ClientSession) -> T,
): T = (
  useSingleDocTransaction { session, abort -> block(session) }
)
suspend inline fun <T> useSingleDocTransaction(
  block: (session: ClientSession, abort: suspend () -> Unit) -> T,
): T {
  val transactionOpts = TransactionOptions.builder()
    .readPreference(ReadPreference.primary())
    .readConcern(ReadConcern.LOCAL)
    .writeConcern(WriteConcern.MAJORITY.withJournal(true))
    .build()
  return mongo.startSession().use { session ->
    var aborted = false
    suspend fun abort() { session.abortTransaction(); aborted = true }
    session.startTransaction(transactionOpts)
    val result = block(session, ::abort)
    if (aborted) throw TransactionAbortedException()
    session.commitTransaction()
    result
  }
}

class TransactionAbortedException : IllegalStateException()
