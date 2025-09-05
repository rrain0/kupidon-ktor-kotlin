package com.rrain.kupidon.routes.routes.http.test

import com.rrain.utils.base.file.FileUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay



private fun getBanSmirksImgStream() = FileUtils.getResourceAsStream(
  "static/Ban smirks  Nanatsu no Taizai.jpg"
)
private fun getZeroTwoImgStream() = FileUtils.getResourceAsStream(
  "static/#zerotwo #ava #top #chan 5949c006e568394a2ddd56143ed94c25.jpg"
)


fun Application.addImgTestRoutes() {
  routing {
    
    // http://localhost:40019/test/image/ban.jpg
    get("/test/image/ban.jpg") {
      val banImgStream = getBanSmirksImgStream()
      
      call.caching = CachingOptions(CacheControl.NoStore(null))
      call.respondOutputStream(
        status = HttpStatusCode.OK,
        contentType = ContentType.parse("image/jpg"),
      ) { banImgStream.transferTo(this) }
    }
    // http://localhost:40019/test/image/zerotwo.jpg
    get("/test/image/zerotwo.jpg") {
      val banImgStream = getZeroTwoImgStream()
      
      call.caching = CachingOptions(CacheControl.NoStore(null))
      call.respondOutputStream(
        status = HttpStatusCode.OK,
        contentType = ContentType.parse("image/jpg"),
      ) { banImgStream.transferTo(this) }
    }
    
    
    
    
    var imgError = false
    
    // http://localhost:40019/test/image/errorable/ban.jpg
    get("/test/image/errorable/ban.jpg") {
      
      if (imgError) {
        return@get call.respond(
          HttpStatusCode.InternalServerError,
          "Image error enabled",
        )
      }
      
      val banImgStream = getBanSmirksImgStream()
      
      call.caching = CachingOptions(CacheControl.NoStore(null))
      call.respondOutputStream(
        status = HttpStatusCode.OK,
        contentType = ContentType.parse("image/jpg"),
      ) { banImgStream.transferTo(this) }
    }
    
    get("/test/image/errorable/enable-error") {
      imgError = true
      call.respondText("Image error enabled")
    }
    get("/test/image/errorable/disable-error") {
      imgError = false
      call.respondText("Image error disabled")
    }
    
    
    
    
    
    // http://localhost:40019/test/image/delay/ban.jpg
    get("/test/image/delay/ban.jpg") {
      
      delay(4000)
      
      val banImgStream = getBanSmirksImgStream()
      
      call.caching = CachingOptions(CacheControl.NoStore(null))
      call.respondOutputStream(
        status = HttpStatusCode.OK,
        contentType = ContentType.parse("image/jpg"),
      ) { banImgStream.transferTo(this) }
    }
    
    // http://localhost:40019/test/image/delay-error-404/ban.jpg
    get("/test/image/delay-error-404/ban.jpg") {
      delay(4000)
      return@get call.respond(HttpStatusCode.NotFound)
    }
    
    // http://localhost:40019/test/image/delay-error-500/ban.jpg
    get("/test/image/delay-error-500/ban.jpg") {
      delay(4000)
      return@get call.respond(HttpStatusCode.InternalServerError)
    }
    
    
    
  }
}