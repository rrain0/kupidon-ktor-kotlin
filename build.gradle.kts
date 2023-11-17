val ktor_version: String by project
val kotlin_version: String by project
val kotlin_coroutines_version: String by project
val logback_version: String by project
val exposed_version : String by project
val h2_version : String by project
val r2dbc_postgresql_version : String by project
val r2dbc_pool_version : String by project
val postgresql_version : String by project
val kotlin_reactor_extensions_version : String by project
val hikari_version : String by project
val jackson_version : String by project



plugins {
  val kotlin_v = "1.9.20"
  val ktor_v = "2.3.6"
  kotlin("jvm") version kotlin_v
  id("io.ktor.plugin") version ktor_v
  id("org.jetbrains.kotlin.plugin.serialization") version kotlin_v
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
  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
  
  //implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  //implementation("io.ktor:ktor-serialization-gson:$ktor_version")
  implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
  // Kotlin Jackson Support
  // https://github.com/FasterXML/jackson-module-kotlin
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
  // Java Time Jackson Support
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")
  
  
  
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
  
  
  
  // -------remove
  implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
  // https://mvnrepository.com/artifact/org.jetbrains.exposed/exposed-java-time
  implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
  implementation("org.postgresql:postgresql:$postgresql_version")
  implementation("com.zaxxer:HikariCP:$hikari_version")
  
  implementation("com.h2database:h2:$h2_version")
  // --------end
  
  
  // R2DBC with PostgreSQL & Kotlin
  implementation("io.r2dbc:r2dbc-postgresql:$r2dbc_postgresql_version")
  implementation("io.r2dbc:r2dbc-pool:$r2dbc_pool_version")
  //implementation("org.postgresql:r2dbc-postgresql:1.0.1.RELEASE") // драйвер от команды спринга, работа с DatabaseClient вместо пула соединений
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$kotlin_reactor_extensions_version")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlin_coroutines_version")
  
  
  // for using of x-forwarded (and forwarded) headers of proxy server
  implementation("io.ktor:ktor-server-forwarded-header:$ktor_version")
  
  implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-partial-content-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-caching-headers-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-resources:$ktor_version")
  implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-jetty-jvm:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
  
  
  // mail sending
  implementation("org.apache.commons:commons-email:1.5")
}