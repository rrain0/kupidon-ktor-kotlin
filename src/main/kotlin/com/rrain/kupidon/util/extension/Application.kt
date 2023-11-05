package com.rrain.kupidon.util.extension

import io.ktor.server.config.*


operator fun ApplicationConfig.get(prop: String) = this.property(prop).getString()