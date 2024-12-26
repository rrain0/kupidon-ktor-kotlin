package com.rrain.kupidon

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*



// TODO explore
class ApplicationTest {
    @Test
    fun testKtorHello() = testApplication {
        application {
            module()
        }
        client.get("/ktor/hello").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
