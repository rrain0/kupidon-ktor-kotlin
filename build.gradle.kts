// fix for java.lang.NoClassDefFoundError: Could not initialize class org.eclipse.jetty.server.HttpConnection
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlinVer: String by project
val ktorVer: String by project
val mongoKotlinCoroutineVer : String by project
val logbackVer: String by project
val jacksonVer : String by project



plugins {
  val kotlinV = "1.9.23"
  val ktorV = "2.3.10"
  
  kotlin("jvm") version kotlinV
  id("io.ktor.plugin") version ktorV
  id("org.jetbrains.kotlin.plugin.serialization") version kotlinV
}

// fix for java.lang.NoClassDefFoundError: Could not initialize class org.eclipse.jetty.server.HttpConnection
tasks.withType<ShadowJar> {
  mergeServiceFiles()
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
  
}