package com.rrain.kupidon.util

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*



fun zonedNow() = ZonedDateTime.now(ZoneOffset.UTC) // this is just offset, without timezone daylight saving rules


/*
  Application common ZonedDateTime format
  
  JavaScript Date works fine:
    new Date('2020-08-26T06:53:27.609+07:30')
    => Wed Aug 26 2020 07:23:27 GMT+0800 (Иркутск, стандартное время)

  Postgres timestamp with time zone works fine:
    timestamptz '2020-08-26T06:53:27.609+07:30'
    => 2020-08-26 07:23:27.609+08
    
    timestamptz'2023-11-10T08:32:55.798Z'
    => 2023-11-10 16:32:55.798+08
 */
val zonedDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" // 2020-08-26T06:53:27.609+00:00
val zonedDateTimeFormatter: DateTimeFormatter =
  DateTimeFormatter.ofPattern(zonedDateTimePattern, Locale.ENGLISH)



fun String.toZonedDateTime() = ZonedDateTime.parse(this, zonedDateTimeFormatter)





fun main(){
  println("toZonedDateTime():")
  println("2020-08-26T06:53:27.609+00:01".toZonedDateTime())
  println("2020-08-26T06:53:27.609-09:11".toZonedDateTime())
  println("2020-08-26T06:53:27.609Z".toZonedDateTime())
  // parsing error
  if (false) println("2011-12-03T10:15:30+01:00[Europe/Paris]".toZonedDateTime())
  
  
  println("DateTimeFormatter.ISO_DATE_TIME")
  println(ZonedDateTime.parse(
    "2023-11-10T08:32:55.798Z", DateTimeFormatter.ISO_DATE_TIME
  ))
  println(ZonedDateTime.parse(
    "2020-08-26T06:53:27.609+00:01", DateTimeFormatter.ISO_DATE_TIME
  ))
  println(ZonedDateTime.parse(
    "2011-12-03T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_DATE_TIME
  ))
  // parsing error - no time zone
  if (false) println(ZonedDateTime.parse(
    "2020-08-26T06:53:27.609", DateTimeFormatter.ISO_DATE_TIME
  ))
  
  /*run {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ:ZZ"  // 2020-08-26 06:53:27.609+00:00
    println("""pattern "$pattern":""")
    val formatter1 = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
    // parsing error
    if (false) println(ZonedDateTime.parse(
      "2023-11-10T08:32:55.798Z", formatter1
    ))
    // parsing error
    if (false) println(ZonedDateTime.parse(
      "2020-08-26T06:53:27.609+00:01", formatter1
    ))
  }*/
  
  run {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    println("""pattern "$pattern":""")
    val formatter = DateTimeFormatter.ofPattern(pattern,Locale.ENGLISH)
    println(ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter))
    println(
      ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter).toLocalDate()
    )
    println(
      ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter)
        .withZoneSameInstant(ZoneId.of("+01:00"))
        .toLocalDate()
    )
  }
  
  run {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    println("""pattern "$pattern":""")
    val formatter = DateTimeFormatter.ofPattern(pattern,Locale.ENGLISH)
    run{
      val date1 = ZonedDateTime.parse("2019-08-26T00:25:00.609+09:00", formatter)
      val date2 = ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter)
      val years = ChronoUnit.YEARS.between(date1,date2)
      println("years between: $years") // 1
    }
    run{
      val date1 = ZonedDateTime.parse("2019-08-26T00:25:00.610+09:00", formatter)
      val date2 = ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter)
      val years = ChronoUnit.YEARS.between(date1,date2)
      println("years between: $years") // 0
    }
    run{
      val date1 = ZonedDateTime.parse("2019-08-26T00:25:00.609+09:00", formatter)
      val date2 = ZonedDateTime.parse("2020-10-26T00:25:00.609+09:00", formatter)
      val years = ChronoUnit.YEARS.between(date1,date2)
      println("years between: $years") // 1
    }
  }
  run {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    val formatter = DateTimeFormatter.ofPattern(pattern,Locale.ENGLISH)
    val nowWith8Zone = zonedNow()
      .withZoneSameInstant(ZoneId.of("+08:00"))
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0)
    println("nowWith8Zone ${formatter.format(nowWith8Zone)}")
  }
}