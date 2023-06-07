package com.rrain.kupidon.service

import com.rrain.kupidon.service.db.RoleDbService
import com.rrain.kupidon.service.db.UserDbService
import com.rrain.kupidon.util.get
import io.ktor.server.application.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


fun Application.configureDatabaseService(){
  
  val appConfig = environment.config
  
  // https://github.com/pgjdbc/r2dbc-postgresql/blob/main/README.md
  // https://www.postgresql.org/docs/current/runtime-config-client.html
  val postgresConnectionFactory = PostgresqlConnectionFactory(
    PostgresqlConnectionConfiguration.builder()
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
    .maxIdleTime(10.seconds.toJavaDuration())
    .maxSize(20)
    .build()
  
  // TODO close pool when application is stopping
  val pool = ConnectionPool(dbConnectionPoolConfig)
  DatabaseService.setConnectionPool(pool)
  
}


object DatabaseService {
  
  lateinit var pool: ConnectionPool
  
  lateinit var  roleServ: RoleDbService
  lateinit var  userServ: UserDbService
  
  fun setConnectionPool(pool: ConnectionPool){
    this.pool = pool
    roleServ = RoleDbService(pool)
    userServ = UserDbService(pool)
  }
  
}