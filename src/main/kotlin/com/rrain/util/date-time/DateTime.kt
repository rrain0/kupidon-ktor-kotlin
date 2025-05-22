package com.rrain.util.`date-time`

import kotlinx.datetime.*


fun zonedNow(): java.time.ZonedDateTime = (
  // this is just offset, without timezone daylight saving rules
  java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
)


/*
  Application common ZonedDateTime format
  
  JavaScript Date works fine:
    new Date('2020-08-26T06:53:27.609+07:30')
    => Wed Aug 26 2020 07:23:27 GMT+0800 (Иркутск, стандартное время)

  Postgres timestamp with time zone works fine:
    timestamptz'2020-08-26T06:53:27.609+07:30'
    => 2020-08-26 07:23:27.609+08
    
    timestamptz'2023-11-10T08:32:55.798Z'
    => 2023-11-10 16:32:55.798+08
 */
const val zonedDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" // 2020-08-26T06:53:27.609+00:00
val zonedDateTimeFormatter: java.time.format.DateTimeFormatter = (
  java.time.format.DateTimeFormatter.ofPattern(zonedDateTimePattern, java.util.Locale.ENGLISH)
)



fun String.toZonedDateTime() = (
  java.time.ZonedDateTime.parse(this, zonedDateTimeFormatter)
)




fun java.time.ZonedDateTime.toTimestamp(): Long = (
  this.toInstant().toEpochMilli()
)

fun Long.toZonedDateTime(): java.time.ZonedDateTime = (
  java.time.ZonedDateTime.ofInstant(
    java.time.Instant.ofEpochMilli(this),
    java.time.ZoneId.of("UTC")
  )
)





fun getAge(birthDate: java.time.LocalDate, timeZone: TimeZone = TimeZone.of("America/New_York")): Int {
  val startDateTime = LocalDateTime(
    year = birthDate.year, monthNumber = birthDate.monthValue, dayOfMonth = birthDate.dayOfMonth,
    hour = 0, minute = 0, second = 0, nanosecond = 0,
  )
  val startInstant = startDateTime.toInstant(timeZone)
  val endInstant = Clock.System.now()
  val yearsBetweenInNewYork = startInstant.yearsUntil(endInstant, timeZone)
  return yearsBetweenInNewYork
}






val localDateFormat: java.time.format.DateTimeFormatter = (
  java.time.format.DateTimeFormatter.ofPattern(
    "yyyy-MM-dd",  // 2020-08-26
    java.util.Locale.ENGLISH
  )
)


fun String.toLocalDate(): java.time.LocalDate = (
  java.time.LocalDate.parse(this, localDateFormat)
)
fun java.time.LocalDate.toFormattedString() = (
  this.format(localDateFormat)
)
















fun main() {
  
  run {
    println("LocalDate test")
    // ok: 1997-11-02
    println("1997-11-02".toLocalDate())
    
    // Exception in thread "main" java.time.format.DateTimeParseException:
    // Text '1997-11-2' could not be parsed at index 8
    println("1997-11-2".toLocalDate())
    
    println()
    println()
    println()
  }
  
  
  
  println("toZonedDateTime():")
  println("2020-08-26T06:53:27.609+00:01".toZonedDateTime())
  println("2020-08-26T06:53:27.609-09:11".toZonedDateTime())
  println("2020-08-26T06:53:27.609Z".toZonedDateTime())
  // parsing error
  if (false) println("2011-12-03T10:15:30+01:00[Europe/Paris]".toZonedDateTime())
  
  
  println("DateTimeFormatter.ISO_DATE_TIME")
  println(java.time.ZonedDateTime.parse(
    "2023-11-10T08:32:55.798Z", java.time.format.DateTimeFormatter.ISO_DATE_TIME
  ))
  println(java.time.ZonedDateTime.parse(
    "2020-08-26T06:53:27.609+00:01", java.time.format.DateTimeFormatter.ISO_DATE_TIME
  ))
  println(java.time.ZonedDateTime.parse(
    "2011-12-03T10:15:30+01:00[Europe/Paris]", java.time.format.DateTimeFormatter.ISO_DATE_TIME
  ))
  // parsing error - no time zone
  if (false) println(java.time.ZonedDateTime.parse(
    "2020-08-26T06:53:27.609", java.time.format.DateTimeFormatter.ISO_DATE_TIME
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
    val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH)
    println(java.time.ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter))
    println(
      java.time.ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter).toLocalDate()
    )
    println(
      java.time.ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter)
        .withZoneSameInstant(java.time.ZoneId.of("+01:00"))
        .toLocalDate()
    )
  }
  
  run {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    println("""pattern "$pattern":""")
    val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH)
    run{
      val date1 = java.time.ZonedDateTime.parse("2019-08-26T00:25:00.609+09:00", formatter)
      val date2 = java.time.ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter)
      val years = java.time.temporal.ChronoUnit.YEARS.between(date1,date2)
      println("years between: $years") // 1
    }
    run{
      val date1 = java.time.ZonedDateTime.parse("2019-08-26T00:25:00.610+09:00", formatter)
      val date2 = java.time.ZonedDateTime.parse("2020-08-26T00:25:00.609+09:00", formatter)
      val years = java.time.temporal.ChronoUnit.YEARS.between(date1,date2)
      println("years between: $years") // 0
    }
    run{
      val date1 = java.time.ZonedDateTime.parse("2019-08-26T00:25:00.609+09:00", formatter)
      val date2 = java.time.ZonedDateTime.parse("2020-10-26T00:25:00.609+09:00", formatter)
      val years = java.time.temporal.ChronoUnit.YEARS.between(date1,date2)
      println("years between: $years") // 1
    }
  }
  run {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH)
    val nowWith8Zone = zonedNow()
      .withZoneSameInstant(java.time.ZoneId.of("+08:00"))
      .withHour(0)
      .withMinute(0)
      .withSecond(0)
      .withNano(0)
    println("nowWith8Zone ${formatter.format(nowWith8Zone)}")
  }
}