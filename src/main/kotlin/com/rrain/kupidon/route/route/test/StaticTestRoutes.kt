package com.rrain.kupidon.route.route.test

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File



fun Application.configureStaticRoutes() {
  
  routing {
    
    
    
    // serves static content from application resources folder:
    // <project-folder>/src/main/resources
    staticResources(
      // http://localhost:40040/static/......
      // http://localhost:40040/static/index.html
      // http://localhost:40040/static/ban.jpg
      "/static", // url base path
      // <project-folder>/src/main/resources/static
      "static" // subfolder in resources folder
    )
    
    
    
    
    
    // serves static content from application working directory:
    // <project-folder>
    staticFiles(
      // http://localhost:40040/static-files/......
      // http://localhost:40040/static-files/ban.jpg
      // http://localhost:40040/static-files/index.html
      "/static-files",
      // <project-folder>/build/resources/main/static
      File("build/resources/main/static")
    )
    
    // working directory is project root:
    // D:\PROG\Kotlin\[projects]\kupidon-ktor-kotlin
    //println("working dir: ${File("").absolutePath}")
    
    
    
  }
  
}