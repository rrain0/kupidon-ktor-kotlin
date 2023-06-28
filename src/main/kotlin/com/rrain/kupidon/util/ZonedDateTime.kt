package com.rrain.kupidon.util

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*



fun zonedNow() = ZonedDateTime.now(ZoneOffset.UTC) // this is just offset, without timezone daylight saving rules


/*
  Application common ZonedDateTime format
  
  JavaScript Date works fine:
    new Date('2020-08-26 06:53:27.609+0730')
    => Wed Aug 26 2020 07:23:27 GMT+0800 (Иркутск, стандартное время)

  Postgres timestamp with time zone works fine:
    timestamptz '2020-08-26 06:53:27.609+0730'
    => 2020-08-26 07:23:27.609+08
 */
val zonedDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern(
  "yyyy-MM-dd HH:mm:ss.SSSZ",  // 2020-08-26 06:53:27.609+0000
  Locale.ENGLISH
)


fun String.toZonedDateTime() = ZonedDateTime.parse(this, zonedDateTimeFormat)





fun main(){
  println("2020-08-26 06:53:27.609+0001".toZonedDateTime())
}