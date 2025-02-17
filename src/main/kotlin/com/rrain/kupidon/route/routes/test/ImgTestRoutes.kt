package com.rrain.kupidon.route.routes.test

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File




fun Application.configureImgTestRoutes() {
  
  routing {
    
    var imgError = false
    
    // http://localhost:40019/test/image/ban.jpg
    get("/test/image/ban.jpg") {
      
      if (imgError) {
        return@get call.respond(
          HttpStatusCode.InternalServerError,
          "Image error enabled"
        )
      }
      
      // <project-folder>/build/resources/main/static/ban.jpg
      // is built from
      // <project-folder>/src/main/resources/static/ban.jpg
      val banImg = File("build/resources/main/static/ban.jpg")
      
      call.respondBytes(
        contentType = ContentType.fromFileExtension(banImg.extension)[0],
        status = HttpStatusCode.OK,
        suspend { banImg.readBytes() }
      )
    }
    
    get("/test/image/error/enable") {
      imgError = true
      call.respondText("Image error enabled")
    }
    get("/test/image/error/disable") {
      imgError = false
      call.respondText("Image error disabled")
    }
    
    
    
    
    
    // http://localhost:40019/test/image/greek-man.png
    get("/test/image/greek-man.png") {
      
      // <project-folder>/build/resources/main/static/greek man IMG_20230922_094037_882 #top.png
      // is built from
      // <project-folder>/src/main/resources/static/greek man IMG_20230922_094037_882 #top.png
      val greekManImg =
        File("build/resources/main/static/greek man IMG_20230922_094037_882 #top.png")
      
      call.caching = CachingOptions(CacheControl.NoStore(null))
      call.respondBytes(
        contentType = ContentType.fromFileExtension(greekManImg.extension)[0],
        status = HttpStatusCode.OK,
        suspend { greekManImg.readBytes() }
      )
    }
  }
  
}