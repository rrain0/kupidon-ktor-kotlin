package com.rrain.kupidon.service.db.mongo

import com.mongodb.*
import com.mongodb.kotlin.client.coroutine.ClientSession
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
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
  
  
  val connectionString = URLBuilder(
    "mongodb://$host:$port/" +
      "?replicaSet=$rs&tls=true&authSource=${'$'}external&authMechanism=MONGODB-X509"
  ).buildString()
  
  
  // https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/connection/connection-options/
  val connectionSettings = MongoClientSettings.builder()
    .applicationName(appName)
    .applyConnectionString(ConnectionString(connectionString))
    .applyToSslSettings { builder ->
      builder.enabled(true)
      builder.context(getSslContext(backendClientCert, caCert))
    }
    .uuidRepresentation(UuidRepresentation.STANDARD)
    .codecRegistry(codecRegistry)
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




fun getSslContext(backendClientCert: String, caCert: String) = run {
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
  val dbName get() = config.dbName
  
  val db get() = client.db(dbName)
  fun db(dbName: String) = client.db(dbName)
}

fun mongo() = MongoDbService.client

val MongoClient.db get() = this.getDatabase(MongoDbService.dbName)

fun MongoClient.db(dbName: String) = this.getDatabase(dbName)

inline fun <reified T : Any> MongoDatabase.coll(collName : String) = (
  this.getCollection<T>(collName)
)




suspend inline fun <T> MongoClient.useTransaction(
  block: (session: ClientSession) -> T,
): T {
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
