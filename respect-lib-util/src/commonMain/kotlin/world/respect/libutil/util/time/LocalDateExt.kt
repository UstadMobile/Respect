package world.respect.libutil.util.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun LocalDate.atStartOfDayInMillisUtc(): Long {
    return atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

fun String.toFormattedDate(): String = try {
    val dt = Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dt.month.number}/${dt.day}/${dt.year}, ${
        dt.hour.toString().padStart(2, '0')
    }:${dt.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    println("Date parsing failed: ${e.message}")
    this
}