package com.rrain.kupidon.util

import java.time.format.DateTimeFormatter
import java.util.*


val localDateFormat = DateTimeFormatter.ofPattern(
  "yyyy-MM-dd",  // 2020-08-26
  Locale.ENGLISH
)