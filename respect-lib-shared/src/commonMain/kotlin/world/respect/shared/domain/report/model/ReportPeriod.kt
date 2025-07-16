package world.respect.shared.domain.report.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import world.respect.libutil.ext.UNSET_DISTANT_FUTURE
import world.respect.libutil.ext.atEndOfDayIn
import world.respect.shared.generated.resources.Res
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import world.respect.shared.generated.resources.*

/**
 * Enum of the relative units that can be selected for a report e.g. last x days, last x months, etc.
 */
enum class ReportTimeRangeUnit(
    override val label: StringResource,
    val unit: DateTimeUnit.DateBased
): OptionWithLabelStringResource {
    DAY(Res.string.day, DateTimeUnit.DAY),
    WEEK(Res.string.weeks, DateTimeUnit.WEEK),
    MONTH(Res.string.months, DateTimeUnit.MONTH),
    YEAR(Res.string.year, DateTimeUnit.YEAR),
}

/**
 *
 */
enum class ReportPeriodOption(
    val period: ReportPeriod,
    override val label: StringResource
): OptionWithLabelStringResource {

    LAST_WEEK(RelativeRangeReportPeriod(ReportTimeRangeUnit.WEEK, 1), Res.string.last_week),

    LAST_30_DAYS(RelativeRangeReportPeriod(ReportTimeRangeUnit.DAY, 30), Res.string.last_30_days),

    LAST_3_MONTHS(RelativeRangeReportPeriod(ReportTimeRangeUnit.MONTH, 3), Res.string.last_3_months),

    CUSTOM_PERIOD(RelativeRangeReportPeriod(ReportTimeRangeUnit.DAY, 1), Res.string.custom_period),

    CUSTOM_DATE_RANGE(FixedReportTimeRange(0L, UNSET_DISTANT_FUTURE), Res.string.custom_date_range),
}

/**
 * Sealed class that represents a report period as selected by the user.
 *
 * When a report is run, the time range will always be from 00:00.00 (midnight) on the first day (
 * as per RunReportRequest.timeZone) until 23:59.999 (end of day) on the last day of the
 * report period (inclusive).
 */
@Serializable
sealed class ReportPeriod {

    /**
     * The start date of the period (inclusive)
     */
    abstract fun periodStart(timeZone: TimeZone): LocalDate

    /**
     * The end date of the period (inclusive)
     */
    abstract fun periodEnd(timeZone: TimeZone): LocalDate

    /**
     * The start time of the period (Instant) as selected by the user.
     *
     * @param timeZone report timezone to use for time calculations as per ReportPeriod doc.
     */
    @OptIn(ExperimentalTime::class)
    fun periodStartInstant(timeZone: TimeZone): Instant {
        return periodStart(timeZone).atStartOfDayIn(timeZone)
    }

    /**
     * The start time of the period (in ms since epoch) as selected by the user.
     *
     * @param timeZone report timezone to use for time calculations as per ReportPeriod doc.
     */
    @OptIn(ExperimentalTime::class)
    fun periodStartMillis(timeZone: TimeZone): Long {
        return periodStartInstant(timeZone).toEpochMilliseconds()
    }

    /**
     * The end time of the range (Instant) as selected by user
     *
     * @param timeZone report timezone to use for time calculations as per ReportPeriod doc.
     */
    @OptIn(ExperimentalTime::class)
    fun periodEndInstant(timeZone: TimeZone): Instant {
        return periodEnd(timeZone).atEndOfDayIn(timeZone)
    }

    /**
     * The end time of the range (in ms since epoch)
     *
     * @param timeZone report timezone to use for time calculations as per ReportPeriod doc.
     */
    @OptIn(ExperimentalTime::class)
    fun periodEndMillis(timeZone: TimeZone): Long {
        return periodEndInstant(timeZone).toEpochMilliseconds()
    }

}

/**
 * A relative report time range is relative to the current time - e.g. last x days, last y months, etc.
 *
 * @param rangeUnit The unit of time to subtract from the current time
 * @param rangeQuantity The quantity of the unit to subtract from the current time
 */
@Serializable
class RelativeRangeReportPeriod(
    val rangeUnit: ReportTimeRangeUnit,
    val rangeQuantity: Int,
): ReportPeriod() {

    @OptIn(ExperimentalTime::class)
    override fun periodEnd(timeZone: TimeZone): LocalDate {
        return Clock.System.now().toLocalDateTime(timeZone).date
    }

    @OptIn(ExperimentalTime::class)
    override fun periodStart(timeZone: TimeZone): LocalDate {
        val nowDateTime = Clock.System.now().toLocalDateTime(timeZone)

        return when(rangeUnit) {
            ReportTimeRangeUnit.YEAR -> {
                nowDateTime.date.minus(rangeQuantity - 1, DateTimeUnit.YEAR).let {
                    LocalDate(it.year, 1, 1)
                }
            }

            ReportTimeRangeUnit.MONTH -> {
                nowDateTime.date.minus(rangeQuantity - 1, DateTimeUnit.MONTH).let {
                    LocalDate(it.year, it.monthNumber, 1)
                }
            }

            ReportTimeRangeUnit.DAY, ReportTimeRangeUnit.WEEK -> {
                /*
                 * Because reports are always run from 00:00.00 to 23:59.999 as per the request timezone,
                 * we always need to add one day - e.g. if a report is for the last 1 day, then from=to,
                 * however when the report runs from will be set to 00:00.000 and to will be set to
                 * 23:59.999 for the current day.
                 */
                return nowDateTime.date.minus(rangeQuantity, rangeUnit.unit)
                    .plus(DatePeriod(days = 1))
            }
        }
    }
}

/**
 * A fixed report date range as specified by the user.
 *
 * @param fromDateMillis the fixed start date of the report, in ms since epoch. When the user selects
 *        a date, it should convert to the timestamp as UTC (e.g. the dates themselves should remain
 *        constant, even if the timezone changes).
 * @param toDateMillis the fixed end date of the report, in ms since epoch. When the user selects
 *        a date, it should convert to the timestamp as UTC (e.g. the dates themselves should remain
 *        constant, even if the timezone changes).
 */
@Serializable
class FixedReportTimeRange(
    val fromDateMillis: Long,
    val toDateMillis: Long,
): ReportPeriod() {

    @OptIn(ExperimentalTime::class)
    override fun periodStart(timeZone: TimeZone): LocalDate {
        return Instant.fromEpochMilliseconds(fromDateMillis)
            .toLocalDateTime(TimeZone.UTC).date
    }

    @OptIn(ExperimentalTime::class)
    override fun periodEnd(timeZone: TimeZone): LocalDate {
        return Instant.fromEpochMilliseconds(toDateMillis)
            .toLocalDateTime(TimeZone.UTC).date
    }

}
