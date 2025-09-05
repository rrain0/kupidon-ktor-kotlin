package com.rrain.utils.ktor.application

import io.ktor.server.application.*
import io.ktor.server.config.*



val Application.appConfig: ApplicationConfig get() = environment.config

operator fun ApplicationConfig.get(prop: String): String = this.property(prop).getString()



