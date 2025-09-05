package com.rrain.kupidon.routes.routes.http.`app-pwa-manifest`

import com.rrain.kupidon.routes.routes.http.`app-api-v1`.ApiV1Routes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.collections.get




fun Application.addPwaManifestRoute() {
  routing {
    get(ApiV1Routes.pwaManifest) {
      
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
      
      //println("call.parameters ${call.parameters}")
      
      val defaultLang = "en-US"
      val localizationMap = mapOf(
        "en-US" to mapOf(
          "lang" to "en-US",
          "short_name" to "Kupidon",
          "name" to "Kupidon - date app",
          "description" to "Kupidon is a dating and relationship app. We offer users a selection of date ideas and places to make their meetings unforgettable.",
        ),
        "ru-RU" to mapOf(
          "lang" to "ru-RU",
          "short_name" to "Купидон",
          "name" to "Купидон - приложение для свиданий",
          "description" to "Купидон — это приложение для знакомств и укрепления отношений. Мы предлагаем пользователям подборки идей и мест для свиданий, чтобы сделать их встречи незабываемыми.",
        ),
      )
      
      val manifest = mutableMapOf(
        *localizationMap[defaultLang]!!.entries.map { it.toPair() }.toTypedArray(),
        "start_url" to ".",
        "display" to "standalone",
        "orientation" to "portrait",
        
        "theme_color" to "#282c34",
        "background_color" to "#282c34",
        
        "icons" to mutableListOf(
          mutableMapOf(
            "src" to "public/assets/icon192.png",
            "type" to "image/png",
            "sizes" to "192x192",
          ),
          mutableMapOf(
            "src" to "public/assets/icon512.png",
            "type" to "image/png",
            "sizes" to "512x512",
          ),
        ),
      )
      
      
      val searchParams = call.request.queryParameters
      
      val baseId = "kupidon-react-pwa"
      val nodeEnvMap = mapOf(
        "development" to mapOf(
          "id" to "$baseId-dev",
        ),
        "production" to mapOf(
          "id" to "$baseId-prod",
        ),
      )
      
      
      val nodeEnv = searchParams["nodeEnv"]
      if (nodeEnv in nodeEnvMap) manifest.putAll(nodeEnvMap[nodeEnv]!!)
      
      val lang = searchParams["lang"]
      if (lang in localizationMap) manifest.putAll(localizationMap[lang]!!)
      
      if (nodeEnv == "development") {
        manifest["short_name"] = "Dev ${manifest["short_name"]}"
        manifest["name"] = "Dev ${manifest["name"]}"
        manifest["description"] = "Dev ${manifest["description"]}"
      }
      
      
      call.respond(manifest)
    }
  }
}