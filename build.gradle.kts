
plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.rrain.kupidon"
version = "0.0.1"

application {
  mainClass.set("io.ktor.server.jetty.jakarta.EngineMain")
  
  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
  mavenCentral()
}

dependencies {
  // Kotlin
  // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
  implementation(libs.kotlin.coroutines.core)
  
  // Ktor server
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.jetty.jakarta)
  
  // SLF4J - Simple Logging Facade for Java
  implementation(libs.slf4j.api)
  implementation(libs.jcl.over.slf4j)
  implementation(libs.logback.core)
  implementation(libs.logback.classic)
  
  // Content negotiation & Jackson
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.serialization.jackson)
  // Kotlin Jackson Support
  // https://github.com/FasterXML/jackson-module-kotlin
  implementation(libs.jackson.module.kotlin)
  // Java Time Jackson Support
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
  implementation(libs.jackson.java.time)
  
  // Auth
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.auth.jwt)
  
  // Other ktor plugins
  implementation(libs.ktor.websocket)
  // Use proxy server forwarded & x-forwarded headers
  implementation(libs.ktor.forwarded.headers)
  implementation(libs.ktor.caching.headers)
  implementation(libs.ktor.auto.head.response)
  implementation(libs.ktor.status.pages)
  implementation(libs.ktor.call.id)
  implementation(libs.ktor.call.logging)
  
  // Kotlin coroutine MongoDB driver
  implementation(libs.mongodb.driver.kotlin.coroutine)
  implementation(libs.mongodb.bson.kotlinx)
  
  // Mail sending
  implementation(libs.apache.email)
}