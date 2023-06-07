package com.rrain.kupidon.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


val appZone = ZoneId.of("UTC+0")
fun zonedNow() = ZonedDateTime.now(appZone)



/*
  Application common ZonedDateTime format
  
  JavaScript Date works fine:
    new Date('2020-08-26 06:53:27.609+0730')
    => Wed Aug 26 2020 07:23:27 GMT+0800 (Иркутск, стандартное время)

  Postgres timestamp with time zone works fine:
    timestamptz '2020-08-26 06:53:27.609+0730'
    => 2020-08-26 07:23:27.609+08
 */
val timestamptzFormat = DateTimeFormatter.ofPattern(
  "yyyy-MM-dd HH:mm:ss.SSSZ",  // 2020-08-26 06:53:27.609+0000
  Locale.ENGLISH
)