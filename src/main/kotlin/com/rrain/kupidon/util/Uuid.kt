package com.rrain.kupidon.util

import java.util.*




fun String.toUuid(): UUID = UUID.fromString(this)


const val NilUuidStr = "00000000-0000-0000-0000-000000000000"
val NilUuid: UUID = NilUuidStr.toUuid()