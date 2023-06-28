package com.rrain.kupidon.service

import com.rrain.kupidon.entity.app.Sex
import com.rrain.kupidon.service.db.RoleDbService
import com.rrain.kupidon.service.db.UserDbService
import com.rrain.kupidon.util.cast
import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import io.netty.buffer.ByteBuf
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.Parameter
import io.r2dbc.postgresql.codec.Codec
import io.r2dbc.postgresql.message.Format
import io.r2dbc.postgresql.type.PostgresqlObjectId
import io.r2dbc.postgresql.util.ByteBufUtils
import reactor.core.publisher.Mono
import kotlin.reflect.full.companionObjectInstance
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration





fun Application.configureDatabaseService(){
  
  val appConfig = environment.config
  
  // https://github.com/pgjdbc/r2dbc-postgresql/blob/main/README.md
  // https://www.postgresql.org/docs/current/runtime-config-client.html
  val postgresConnectionFactory = PostgresqlConnectionFactory(
    PostgresqlConnectionConfiguration.builder()
      
      /*.codecRegistrar { connection, allocator, registry ->
        registry.addFirst(object : Codec<Sex>{
          override fun canDecode(dataType: Int, format: Format, type: Class<*>): Boolean =
            dataType==PostgresqlObjectId.VARCHAR.objectId
          
          override fun decode(buffer: ByteBuf?, dataType: Int, format: Format, type: Class<out Sex>): Sex? {
            buffer ?: return null
            return Sex.valueOf(ByteBufUtils.decode(buffer))
          }
          
          override fun canEncodeNull(type: Class<*>): Boolean = false
          
          override fun encodeNull(): Parameter {
            return Parameter(
              Format.FORMAT_TEXT,
              PostgresqlObjectId.VARCHAR.objectId,
              Parameter.NULL_VALUE
            )
          }
          
          override fun canEncode(value: Any): Boolean = value is Sex
          
          override fun encode(value: Any): Parameter {
            return Parameter(Format.FORMAT_TEXT, PostgresqlObjectId.VARCHAR.objectId) {
              ByteBufUtils.encode(allocator, (value as Sex).name)
            }
          }
          
        })
        Mono.empty()
      }*/
      
      /*.codecRegistrar { connection, allocator, registry ->
        registry.addFirst(object : Codec<Boolean>{
          override fun canDecode(dataType: Int, format: Format, type: Class<*>): Boolean =
            dataType==PostgresqlObjectId.BOOL.objectId
          
          override fun decode(buffer: ByteBuf?, dataType: Int, format: Format, type: Class<out Boolean>): Boolean? {
            buffer ?: return null
            return when (val decoded = ByteBufUtils.decode(buffer)){
              "t" -> true
              "f" -> false
              else -> throw IllegalArgumentException("Illegal argument: '$decoded'")
            }
          }
          
          override fun canEncodeNull(type: Class<*>): Boolean = false
          
          override fun encodeNull(): Parameter {
            return Parameter(
              Format.FORMAT_TEXT,
              PostgresqlObjectId.BOOL.objectId,
              Parameter.NULL_VALUE
            )
          }
          
          override fun canEncode(value: Any): Boolean = value is Boolean
          
          override fun encode(value: Any): Parameter {
            return Parameter(Format.FORMAT_TEXT, PostgresqlObjectId.BOOL.objectId) {
              ByteBufUtils.encode(allocator, if (value as Boolean) "t" else "f")
            }
          }
          
        })
        Mono.empty()
      }*/
      
      .applicationName(appConfig["database.connection.application-name"])
      .host(appConfig["database.connection.host"])
      .port(appConfig["database.connection.port"].toInt())
      .username(appConfig["database.connection.user"])
      .password(appConfig["database.connection.pwd"])
      .database(appConfig["database.connection.database"])
      .schema(appConfig["database.connection.schema"])
      //.enableSsl() // Exception in thread "main" io.r2dbc.postgresql.client.AbstractPostgresSSLHandlerAdapter$PostgresqlSslException: Server support for SSL connection is disabled, but client was configured with SSL mode verify-full
      .build()
  )
  
  val dbConnectionPoolConfig = ConnectionPoolConfiguration.builder(postgresConnectionFactory)
    .maxIdleTime(20.seconds.toJavaDuration())
    .maxSize(20)
    .build()
  
  // TODO close pool when application is stopping
  val pool = ConnectionPool(dbConnectionPoolConfig)
  DatabaseService.setConnectionPool(pool)
  
}


object DatabaseService {
  
  lateinit var pool: ConnectionPool
  
  lateinit var roleServ: RoleDbService
  lateinit var userServ: UserDbService
  
  fun setConnectionPool(pool: ConnectionPool){
    this.pool = pool
    roleServ = RoleDbService(pool)
    userServ = UserDbService(pool)
  }
  
}