package com.rrain.kupidon.service.mongo

import com.mongodb.*
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.rrain.kupidon.service.mongo.model.ChatMessageMongo
import com.rrain.kupidon.service.mongo.model.ChatMongo
import com.rrain.kupidon.service.mongo.model.UserMongo
import com.rrain.kupidon.service.mongo.model.UserToUserLikeMongo
import com.rrain.`util-ktor`.application.appConfig
import com.rrain.`util-ktor`.application.get
import com.rrain.util.`date-time`.asTimestampToInstant
import com.rrain.util.`date-time`.toLocalDateInUtc
import com.rrain.util.`date-time`.toTimestamp
import com.rrain.util.`date-time`.toUtcInstant
import com.rrain.util.`date-time`.toZonedDateTime
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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
  // !!! Earlier declarations have higher priority
  CodecRegistries.fromCodecs(
    object : Codec<Instant> {
      override fun getEncoderClass() = Instant::class.java
      override fun encode(writer: BsonWriter, value: Instant, encoderContext: EncoderContext) {
        writer.writeDateTime(value.toTimestamp())
      }
      override fun decode(reader: BsonReader, decoderContext: DecoderContext): Instant {
        return reader.readDateTime().asTimestampToInstant()
      }
    },
    object : Codec<LocalDate> {
      override fun getEncoderClass() = LocalDate::class.java
      override fun encode(writer: BsonWriter, value: LocalDate, encoderContext: EncoderContext) {
        writer.writeDateTime(value.toUtcInstant().toTimestamp())
      }
      override fun decode(reader: BsonReader, decoderContext: DecoderContext): LocalDate {
        return reader.readDateTime().asTimestampToInstant().toLocalDateInUtc()
      }
    },
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

