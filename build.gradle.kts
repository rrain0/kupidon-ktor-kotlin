// fix for java.lang.NoClassDefFoundError: Could not initialize class org.eclipse.jetty.server.HttpConnection
//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlinVer: String by project
val ktorVer: String by project
val mongoKotlinCoroutineVer : String by project
val slf4jVer: String by project
val logbackVer: String by project
val jacksonVer : String by project



plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.plugin.serialization)
}

// fix for java.lang.NoClassDefFoundError: Could not initialize class org.eclipse.jetty.server.HttpConnection
/*tasks.withType<ShadowJar> {
  mergeServiceFiles()
}*/

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
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.jetty.jakarta)
  
  
  
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.serialization.jackson)
  // Kotlin Jackson Support
  // https://github.com/FasterXML/jackson-module-kotlin
  implementation(libs.jackson.module.kotlin)
  
  
  
  // TODO move to Kotlin Time
  // Java Time Jackson Support
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVer")
  
  
  
  implementation("io.ktor:ktor-network-tls-certificates:$ktorVer")
  
  
  
  // Kotlin coroutine MongoDB driver
  implementation("org.mongodb:mongodb-driver-kotlin-coroutine:$mongoKotlinCoroutineVer")
  implementation("org.mongodb:bson-kotlinx:$mongoKotlinCoroutineVer")
  
  
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
  
  
  implementation("io.ktor:ktor-server-websockets-jvm:$ktorVer")
  
  
  // SLF4J - Simple Logging Facade for Java
  implementation("org.slf4j:slf4j-api:$slf4jVer")
  implementation("org.slf4j:jcl-over-slf4j:$slf4jVer")
  implementation("ch.qos.logback:logback-core:$logbackVer")
  implementation("ch.qos.logback:logback-classic:$logbackVer")
  
  
  // Mail sending
  implementation("org.apache.commons:commons-email:1.6.0")
  
  
  // Tests
  testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVer")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVer")
  
  
}