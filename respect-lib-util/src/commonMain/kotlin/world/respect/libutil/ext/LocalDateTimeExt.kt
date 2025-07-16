package world.respect.libutil.ext

import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/**
 * Like atStartOfDayIn(as per Kotlinx DateTime) but for end of day
 */
@OptIn(ExperimentalTime::class)
fun LocalDate.atEndOfDayIn(timeZone: TimeZone) : Instant {
    return atTime(23, 59, 59, 999_999_000).toInstant(timeZone)
}
