package com.rrain.util.ktor.call

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin



// Returns host (domain or ip) by which client requested the server
val ApplicationCall.host get() = this.request.origin.serverHost
// Returns port by which client requested the server
val ApplicationCall.port get() = this.request.origin.serverPort