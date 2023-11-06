package com.rrain.kupidon.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*



val localDateFormat = DateTimeFormatter.ofPattern(
  "yyyy-MM-dd",  // 2020-08-26
  Locale.ENGLISH
)


fun String.toLocalDate() = LocalDate.parse(this, localDateFormat)



fun main(){
  // ok: 1997-11-02
  println("1997-11-02".toLocalDate())
  
  // Exception in thread "main" java.time.format.DateTimeParseException:
  // Text '1997-11-2' could not be parsed at index 8
  println("1997-11-2".toLocalDate())
}