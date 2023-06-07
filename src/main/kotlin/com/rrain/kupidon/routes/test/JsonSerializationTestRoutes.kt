package com.rrain.kupidon.routes.test

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureJsonSerializationTestRoutes() {
  routing {
    
    get("/test/json/map") {
      call.respond(mapOf<Any,Any>(
        "hello" to "world",
        1 to 1,
        2 to "two",
        "three" to 3,
        "yes" to true,
        false to "no",
        "array" to arrayOf("first", 2, false),
        "object" to object { val id = "kjldshnv"; val prop = false },
        "map" to mapOf("red" to "#ff0000", "green" to "#00ff00", "blue" to "#0000ff"),
      ))
      /*
      result:
        {"hello":"world","1":1,"2":"two","three":3,"yes":true,"false":"no","array":["first",2,false],"object":{"id":"kjldshnv","prop":false},"map":{"red":"#ff0000","green":"#00ff00","blue":"#0000ff"}}
       */
    }
    
    data class TestData(
      val intProp: Int,
      val boolProp: Boolean,
      val doubleProp: Double,
      val stringProp: String,
      val arrayProp: List<Any?>,
      val mapProp: Map<String,Any?>,
      val nullProp: Nothing?,
      val urlPathInt: Int? = null,
    )
    /*
    Example:
      {
        "intProp": 5,
        "boolProp": true,
        "doubleProp": 67.35,
        "stringProp": "какая-то строка",
        "arrayProp": [false, 87, "some string", null],
        "mapProp": {
            "id": 78,
            "name": "Lax",
            "hasSuperpowers": true,
            "color": null
        },
        "nullProp": null
    }
     */
    post("/test/json/object/{url-path-int}") {
      var testData = call.receive<TestData>()
      testData = testData.copy(
        urlPathInt = call.parameters["url-path-int"]?.toInt()
      )
      call.respond(testData)
    }
  }
}