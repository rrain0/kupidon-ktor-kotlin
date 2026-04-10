import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinV = "2.2.10"
  val ktorV = "3.2.1"
  
  // Kotlin
  kotlin("jvm") version kotlinV
  
  // Ktor
  id("io.ktor.plugin") version ktorV
}

group = "com.rrain.kupidon"
version = "0.0.1"

application {
  mainClass = "io.ktor.server.jetty.jakarta.EngineMain"
  
  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
  freeCompilerArgs.add("-Xcontext-parameters") // enable experimental context parameters
}


repositories {
  mavenCentral()
}

dependencies {
  // Kotlin Coroutines
  val kotlinCoroutinesV = "1.10.2"
  // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesV")
  // Kotlin DateTime
  val kotlinDateTimeV = "0.6.2"
  // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-datetime
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinDateTimeV")
  
  // Ktor server core
  implementation("io.ktor:ktor-server-core")
  // Ktor server core JVM
  implementation("io.ktor:ktor-server-core-jvm")
  // Ktor server engine
  implementation("io.ktor:ktor-server-jetty-jakarta")
  // To be removed in the future, now it can be necessary for some plugins
  implementation("io.ktor:ktor-server-host-common")
  
  // Ktor server - Content negotiation (response body serialization, request body deserialization, ...)
  implementation("io.ktor:ktor-server-content-negotiation")
  // Ktor serialization via Jackson
  implementation("io.ktor:ktor-serialization-jackson")
  // Kotlin Jackson Support
  val jacksonV = "2.19.0"
  // https://github.com/FasterXML/jackson-module-kotlin
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonV")
  // Java Time Jackson Support
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonV")
  
  // Kotlin coroutine MongoDB driver
  // BOM - Bill of Materials - organizes dependency versions
  // https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/quick-start/
  val mongoKotlinCoroutineDriverV = "5.5.0"
  implementation(platform("org.mongodb:mongodb-driver-bom:$mongoKotlinCoroutineDriverV"))
  implementation("org.mongodb:mongodb-driver-kotlin-coroutine")
  implementation("org.mongodb:bson-kotlinx")
  
  val slf4jV = "2.0.17"
  val logbackV = "1.5.18"
  // SLF4J - Simple Logging Facade for Java
  implementation("org.slf4j:slf4j-api:$slf4jV")
  // Транзитивная зависимость без которой в рантайме может не найтись класс
  implementation("org.slf4j:jcl-over-slf4j:$slf4jV")
  // Транзитивная зависимость без которой в рантайме может не найтись класс
  implementation("ch.qos.logback:logback-core:$logbackV")
  // Транзитивная зависимость без которой в рантайме может не найтись класс
  implementation("ch.qos.logback:logback-classic:$logbackV")
  
  
  // Other ktor plugins
  // Ktor server - Auth
  implementation("io.ktor:ktor-server-auth")
  // Ktor server - JWT auth
  implementation("io.ktor:ktor-server-auth-jwt")
  // Ktor server - Websocket
  implementation("io.ktor:ktor-server-websockets")
  // Ktor server - Server-Sent Events (SSE) - Push server
  implementation("io.ktor:ktor-server-sse")
  // Use proxy server forwarded & x-forwarded headers
  implementation("io.ktor:ktor-server-forwarded-header")
  implementation("io.ktor:ktor-server-caching-headers")
  implementation("io.ktor:ktor-server-auto-head-response")
  implementation("io.ktor:ktor-server-status-pages")
  implementation("io.ktor:ktor-server-call-id")
  implementation("io.ktor:ktor-server-call-logging")
  
  // Mail sending
  val apacheEmailV = "1.6.0"
  implementation("org.apache.commons:commons-email:$apacheEmailV")
}