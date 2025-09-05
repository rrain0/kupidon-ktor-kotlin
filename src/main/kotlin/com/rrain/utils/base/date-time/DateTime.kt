package com.rrain.utils.base.`date-time`

import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char




fun now() = Clock.System.now()
fun Instant.isExpired(now: Instant = now()) = (this - now).inWholeMilliseconds <= 0

fun Instant.toTimestamp() = toEpochMilliseconds()
fun Long.asTimestampToInstant() = Instant.fromEpochMilliseconds(this)

// Example: "2025-06-22T10:57:01.624Z"
val instantOutFormat = DateTimeComponents.Format {
  date(LocalDate.Formats.ISO)
  char('T')
  hour(Padding.ZERO)
  char(':')
  minute(Padding.ZERO)
  char(':')
  second(Padding.ZERO)
  char('.')
  secondFraction(3)
  offset(UtcOffset.Formats.ISO_BASIC)
}
fun Instant.formatToString() = format(instantOutFormat)
fun String.toInstant() = Instant.parse(this)


fun LocalDate.toUtcInstant() = atStartOfDayIn(TimeZone.UTC)
fun LocalDate.toZonedInstant(timeZone: TimeZone) = atStartOfDayIn(timeZone)
fun Instant.toLocalDateInUtc() = toLocalDateTime(TimeZone.UTC).date

// Example: "2025-06-22"
val localDateOutFormat = LocalDate.Formats.ISO
fun LocalDate.formatToString() = format(localDateOutFormat)
fun String.toLocalDate() = LocalDate.parse(this)

fun getAge(birthDate: LocalDate, timeZone: TimeZone): Int = (
  birthDate.toZonedInstant(timeZone).yearsUntil(now(), timeZone)
)
