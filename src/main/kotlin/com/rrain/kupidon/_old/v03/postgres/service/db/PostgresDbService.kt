package com.rrain.kupidon._old.v03.postgres.service.db

import com.rrain.kupidon._old.v03.postgres.service.db.`table-service`.RoleDbService
import com.rrain.kupidon._old.v03.postgres.service.db.`table-service`.TransactionDbService
import com.rrain.kupidon._old.v03.postgres.service.db.`table-service`.UserDbService
import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration





fun Application.configurePostgresDbService(){
  
  val appConfig = environment.config
  
  // https://github.com/pgjdbc/r2dbc-postgresql/blob/main/README.md
  // https://www.postgresql.org/docs/current/runtime-config-client.html
  val postgresConnectionFactory = PostgresqlConnectionFactory(
    PostgresqlConnectionConfiguration.builder()
      
      /*.codecRegistrar { connection, allocator, registry ->
        registry.addFirst(object : Codec<Gender>{
          override fun canDecode(dataType: Int, format: Format, type: Class<*>): Boolean =
            dataType==PostgresqlObjectId.VARCHAR.objectId
          
          override fun decode(buffer: ByteBuf?, dataType: Int, format: Format, type: Class<out Gender>): Gender? {
            buffer ?: return null
            return Gender.valueOf(ByteBufUtils.decode(buffer))
          }
          
          override fun canEncodeNull(type: Class<*>): Boolean = false
          
          override fun encodeNull(): Parameter {
            return Parameter(
              Format.FORMAT_TEXT,
              PostgresqlObjectId.VARCHAR.objectId,
              Parameter.NULL_VALUE
            )
          }
          
          override fun canEncode(value: Any): Boolean = value is Gender
          
          override fun encode(value: Any): Parameter {
            return Parameter(Format.FORMAT_TEXT, PostgresqlObjectId.VARCHAR.objectId) {
              ByteBufUtils.encode(allocator, (value as Gender).name)
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
      
      .applicationName(appConfig["db.connection.application-name"])
      .host(appConfig["db.connection.postgres.host"])
      .port(appConfig["db.connection.postgres.port"].toInt())
      .username(appConfig["db.connection.postgres.user"])
      .password(appConfig["db.connection.postgres.pwd"])
      .database(appConfig["db.connection.postgres.database"])
      .schema(appConfig["db.connection.postgres.schema"])
      //.enableSsl() // Exception in thread "main" io.r2dbc.postgresql.client.AbstractPostgresSSLHandlerAdapter$PostgresqlSslException: Server support for SSL connection is disabled, but client was configured with SSL mode verify-full
      .build()
  )
  
  val dbConnectionPoolConfig = ConnectionPoolConfiguration.builder(postgresConnectionFactory)
    .maxIdleTime(20.seconds.toJavaDuration())
    .maxSize(20)
    .build()
  
  // TODO close pool when application is stopping
  val pool = ConnectionPool(dbConnectionPoolConfig)
  PostgresDbService.setConnectionPool(pool)
  
}


object PostgresDbService {
  
  lateinit var pool: ConnectionPool
  
  lateinit var roleServ: RoleDbService
  lateinit var userServ: UserDbService
  lateinit var transactionDbServ: TransactionDbService
  
  fun setConnectionPool(pool: ConnectionPool){
    PostgresDbService.pool = pool
    roleServ = RoleDbService(pool)
    userServ = UserDbService(pool)
    transactionDbServ = TransactionDbService(pool)
  }
  
}