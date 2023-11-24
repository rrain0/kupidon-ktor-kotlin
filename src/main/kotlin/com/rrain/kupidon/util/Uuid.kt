package com.rrain.kupidon.util

import java.util.*


fun String.toUuid() = UUID.fromString(this)