val kotlinVer: String by project
val ktorVer: String by project
val kotlinCoroutinesReactorVer: String by project
val logbackVer: String by project
val exposedVer : String by project
val h2Ver : String by project
val mongoKotlinCoroutineVer : String by project
val r2dbcPostgresqlVer : String by project
val r2dbcPoolVer : String by project
val postgresqlVer : String by project
val kotlinReactorExtensionsVer : String by project
val hikariVer : String by project
val jacksonVer : String by project



plugins {
  val kotlinV = "1.9.21"
  val ktorV = "2.3.6"
  
  kotlin("jvm") version kotlinV
  id("io.ktor.plugin") version ktorV
  id("org.jetbrains.kotlin.plugin.serialization") version kotlinV
}



group = "com.rrain.kupidon"
version = "0.0.1"
application {
  mainClass.set("io.ktor.server.jetty.EngineMain")
  
  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}



repositories {
  mavenCentral()
}

dependencies {
  implementation("io.ktor:ktor-server-core-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-websockets-jvm:$ktorVer")
  
  
  //implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  //implementation("io.ktor:ktor-serialization-gson:$ktor_version")
  implementation("io.ktor:ktor-serialization-jackson:$ktorVer")
  // Kotlin Jackson Support
  // https://github.com/FasterXML/jackson-module-kotlin
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVer")
  // Java Time Jackson Support
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVer")
  
  
  
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVer")
  implementation("io.ktor:ktor-network-tls-certificates:$ktorVer")
  
  
  
  
  
  // Kotlin coroutine MongoDB driver
  implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongoKotlinCoroutineVer")
  
  
  
  
  // for using of x-forwarded (and forwarded) headers of proxy server
  implementation("io.ktor:ktor-server-forwarded-header:$ktorVer")
  
  implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-call-id-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-partial-content-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-cors-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-caching-headers-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-host-common-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-resources:$ktorVer")
  implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-auth-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVer")
  implementation("io.ktor:ktor-server-jetty-jvm:$ktorVer")
  implementation("ch.qos.logback:logback-classic:$logbackVer")
  testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVer")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVer")
  
  
  // mail sending
  implementation("org.apache.commons:commons-email:1.5")
  
  
  
  
  
  
  // -------old
  // Postgres Exposed
  implementation("org.jetbrains.exposed:exposed-core:$exposedVer")
  implementation("org.jetbrains.exposed:exposed-dao:$exposedVer")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVer")
  // https://mvnrepository.com/artifact/org.jetbrains.exposed/exposed-java-time
  implementation("org.jetbrains.exposed:exposed-java-time:$exposedVer")
  implementation("org.postgresql:postgresql:$postgresqlVer")
  implementation("com.zaxxer:HikariCP:$hikariVer")
  
  // h2 database
  implementation("com.h2database:h2:$h2Ver")
  
  // R2DBC with PostgreSQL & Kotlin
  implementation("io.r2dbc:r2dbc-postgresql:$r2dbcPostgresqlVer")
  implementation("io.r2dbc:r2dbc-pool:$r2dbcPoolVer")
  //implementation("org.postgresql:r2dbc-postgresql:1.0.1.RELEASE") // драйвер от команды спринга, работа с DatabaseClient вместо пула соединений
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$kotlinReactorExtensionsVer")
  // binds r2dbc reactor & kotlin coroutines, allows to use Flux & Mono
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesReactorVer")
  // --------old end
}