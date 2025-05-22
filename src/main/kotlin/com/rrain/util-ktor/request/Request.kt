package com.rrain.`util-ktor`.request

import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest



data class HostPort(val host: String, val port: Int)

// Returns host(domain or ip):port by which client requested the server
fun ApplicationRequest.getHostPort() = HostPort(this.origin.serverHost, this.origin.serverPort)

