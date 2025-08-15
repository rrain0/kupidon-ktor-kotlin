package com.rrain.kupidon.service.env

import com.rrain.util.ktor.application.get
import com.rrain.util.ktor.application.appConfig
import io.ktor.server.application.Application
import kotlin.time.Duration



object Env {
  context(app: Application)
  val isDevelopment: Boolean get() =
    app.appConfig["ktor.development"].toBoolean()
  
  
  context(app: Application)
  val dbConnectionApplicationName: String get() =
    app.appConfig["db.connection.application-name"]
  
  context(app: Application)
  val dbConnectionMongoHost: String get() =
    app.appConfig["db.connection.mongo.host"]
  
  context(app: Application)
  val dbConnectionMongoPort: Int get() =
    app.appConfig["db.connection.mongo.port"].toInt()
  
  context(app: Application)
  val dbConnectionMongoRs: String get() =
    app.appConfig["db.connection.mongo.rs"]
  
  context(app: Application)
  val dbConnectionMongoDatabase: String get() =
    app.appConfig["db.connection.mongo.database"]
  
  context(app: Application)
  val dbConnectionMongoBackendClientCert: String get() =
    app.appConfig["db.connection.mongo.backendClientCert"]
  
  context(app: Application)
  val dbConnectionMongoCaCert: String get() =
    app.appConfig["db.connection.mongo.caCert"]
  
  
  context(app: Application)
  val dbUserPwdHashingAlgorithm: String get() =
    app.appConfig["db.user-pwd-hashing.algorithm"]
  
  context(app: Application)
  val dbUserPwdHashingSecret: String get() =
    app.appConfig["db.user-pwd-hashing.secret"]
  
  context(app: Application)
  val dbUserPwdHashingIterations: Int get() =
    app.appConfig["db.user-pwd-hashing.iterations"].toInt()
  
  context(app: Application)
  val dbUserPwdHashingHashLen: Int get() =
    app.appConfig["db.user-pwd-hashing.hash-len"].toInt()
  
  
  context(app: Application)
  val accessTokenSecret: String get() =
    app.appConfig["jwt.access-token.secret"]
  
  context(app: Application)
  val accessTokenLifetime: Duration get() =
    app.appConfig["jwt.access-token.lifetime"].run(Duration::parse)
  
  
  context(app: Application)
  val refreshTokenSecret: String get() =
    app.appConfig["jwt.refresh-token.secret"]
  
  context(app: Application)
  val refreshTokenLifetime: Duration get() =
    app.appConfig["jwt.refresh-token.lifetime"].run(Duration::parse)
  
  context(app: Application)
  val emailVerifyAccessTokenLifetime: Duration get() =
    app.appConfig["jwt.email-verify-access-token.lifetime"].run(Duration::parse)
  
  
  context(app: Application)
  val mailEmail: String get() =
    app.appConfig["mail.email"]
  
  context(app: Application)
  val mailPwd: String get() =
    app.appConfig["mail.pwd"]
}

