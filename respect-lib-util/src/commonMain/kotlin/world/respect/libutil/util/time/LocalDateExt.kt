package world.respect.libutil.util.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.text.SimpleDateFormat
import java.util.Locale

fun LocalDate.atStartOfDayInMillisUtc(): Long {
    return atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

fun String.toFormattedDate(): String = try {
    SimpleDateFormat("M/d/yyyy, HH:mm", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getDefault()
    }.format(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.parse(this)!!
    )
} catch (e: Exception) { this }
