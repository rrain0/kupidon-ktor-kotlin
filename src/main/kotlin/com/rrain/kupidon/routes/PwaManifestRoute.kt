package com.rrain.kupidon.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


object PwaManifestRoute {
  val manifest = "/manifest.json"
}


fun Application.configurePwaManifestRoute(){
  
  routing {
    
    
    get(PwaManifestRoute.manifest) {
      
      //val headers = call.printHeaders()
      //println("headers: $headers")
      //{
      //  Accept = [text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7]
      //  Connection = [close]
      //  User-Agent = [Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 OPR/104.0.0.0 (Edition developer)]
      //  Sec-Fetch-Site = [none]
      //  Sec-Fetch-Dest = [document]
      //  Host = [localhost:40040]
      //  Accept-Encoding = [gzip, deflate, br]
      //  Sec-Fetch-Mode = [navigate]
      //  sec-ch-ua = ["Opera";v="104", "Not;A=Brand";v="8", "Chromium";v="117"]
      //  sec-ch-ua-mobile = [?0]
      //  Cache-Control = [max-age=0]
      //  Upgrade-Insecure-Requests = [1]
      //  sec-ch-ua-platform = ["Windows"]
      //  Sec-Fetch-User = [?1]
      //  Accept-Language = [ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7]
      //}
      println("call.parameters ${call.parameters}")
      
      
      val manifest = mutableMapOf(
        "lang" to "en-US",
        "name" to "Kupidon",
        "short_name" to "Kupidon",
        "description" to "Kupidon date app",
        "start_url" to ".",
        "display" to "standalone",
        "orientation" to "portrait", // works only when app installed
        
        "theme_color" to "#282c34",
        "background_color" to "#282c34",
        
        "icons" to mutableListOf(
          mutableMapOf(
            "src" to "/logo64.png",
            "type" to "image/png",
            "sizes" to "64x64"
          ),mutableMapOf(
            "src" to "/logo192.png",
            "type" to "image/png",
            "sizes" to "192x192"
          ),mutableMapOf(
            "src" to "/logo512.png",
            "type" to "image/png",
            "sizes" to "512x512" // splashscreen icon
          )
        )
      )
      
      
      val baseId = "kupidon-react-pwa"
      val nodeEnvMap = mapOf(
        "development" to mapOf(
          "id" to "$baseId-development"
        ),
        "production" to mapOf(
          "id" to "$baseId-production"
        ),
      )
      
      val lacalizationMap = mapOf(
        "en-US" to mapOf(
          "lang" to "en-US",
          "name" to "Kupidon",
          "short_name" to "Kupidon",
          "description" to "Kupidon date app",
        ),
        "ru-RU" to mapOf(
          "lang" to "ru-RU",
          "name" to "Купидон",
          "short_name" to "Купидон",
          "description" to "Купидон - приложение для свидания",
        ),
      )
      
      
      val nodeEnv = call.parameters["nodeEnv"]
      if (nodeEnv in nodeEnvMap) manifest.putAll(nodeEnvMap[nodeEnv]!!)
      
      val lang = call.parameters["lang"]
      if (lang in lacalizationMap) manifest.putAll(lacalizationMap[lang]!!)
      
      if (nodeEnv=="development") {
        manifest["name"] = "Dev ${manifest["name"]}"
        manifest["short_name"] = "Dev ${manifest["short_name"]}"
        manifest["description"] = "Dev ${manifest["description"]}"
      }
      
      
      call.respond(manifest)
    }
    
    
  }
  
}