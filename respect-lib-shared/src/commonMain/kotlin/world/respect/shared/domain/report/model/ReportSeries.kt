package world.respect.shared.domain.report.model

import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

@Serializable
data class ReportSeries(

    val reportSeriesUid: Int = 0,

    val reportSeriesTitle: String = "Series 1",

    val reportSeriesYAxis: ReportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,

    val reportSeriesVisualType: ReportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,

    val reportSeriesSubGroup: ReportXAxis? = ReportXAxis.DAY,

    val reportSeriesFilters: List<ReportFilter>? = null,

    )

enum class YAxisTypes(
    override val label: StringResource,
) : OptionWithLabelStringResource {
    COUNT(Res.string.count), DURATION(Res.string.duration),
}

/** Enum representing different Y-axis or report series options */
enum class ReportSeriesYAxis(
    override val label: StringResource,
    val type: YAxisTypes
) : OptionWithLabelStringResource {
    TOTAL_DURATION(Res.string.total_duration, YAxisTypes.DURATION),
    AVERAGE_DURATION(Res.string.average_duration, YAxisTypes.DURATION),
    NUMBER_SESSIONS(Res.string.number_sessions, YAxisTypes.COUNT),
    INTERACTIONS_RECORDED(Res.string.interactions_recorded, YAxisTypes.COUNT),
    NUMBER_ACTIVE_USERS(Res.string.number_active_users, YAxisTypes.COUNT),
    AVERAGE_USAGE_TIME_PER_USER(Res.string.average_usage_time_per_user, YAxisTypes.DURATION),
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(override val label: StringResource) :
    OptionWithLabelStringResource {
    BAR_CHART(Res.string.bar_chart),
    LINE_GRAPH(Res.string.line_chart);
}

/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis(
    override val label: StringResource,
    val personJoinRequired: Boolean = false,
    val datePeriod: DatePeriod? = null,
) : OptionWithLabelStringResource {
    /**
     * Displayed to the user using the localized date formatted for the specified date
     */
    DAY(Res.string.day, datePeriod = DatePeriod(days = 1)),

    /**
     * When report data xAxis is by week, or data is subgrouped by week, this is based on the day of
     * the week of the first day of the reporting period. E.g. if the report period is Tuesday
     * 4/Feb/25 to Monday 17/Feb/25, then there will be two entries on the xAxis: 2025-02-04, and
     * 2025-02-11.
     *
     * Displayed to the user using the localized date formatted for the specified date
     */
    WEEK(Res.string.weekly, datePeriod = DatePeriod(days = 7)),

    /**
     * When report data xAxis is by month, or data is subgrouped by month, this will be done by
     * calendar month. Queries will group data using YYYY-MM-01 e.g. using DATE_TRUNC('month'..)
     * on PostgreSQL and the strftime 'start of month' modifier on SQLite.
     *
     * Displayed to the user as Month - Year using localized date formatter
     */
    MONTH(Res.string.monthly, datePeriod = DatePeriod(months = 1)),

    /**
     * When report data xAxis is by month, or data is subgrouped by month, this will be done by
     * calendar year. Queries will group data using YYYY-01-01 e.g. using DATE_TRUNC('year'..)
     * on PostgreSQL and the strftime 'start of year' modifier on SQLite.
     *
     * Displayed to the user as the year (only)
     */
    YEAR(Res.string.year, datePeriod = DatePeriod(years = 1)),

    /**
     * Displayed to the user as the clazz name. RunReportUseCaseDbImpl will substitute the clazzUid
     * included in the query with the clazz name
     */
    CLASS(Res.string.class_name),

    /**
     * Displayed to the user using the localized string as per their locale (e.g. male, female..)
     */
    GENDER(Res.string.gender_literal, personJoinRequired = true),
}

/** Enum representing different filter types for report series */
enum class FilterType(override val label: StringResource) : OptionWithLabelStringResource {
    PERSON_AGE(Res.string.person_age),
    PERSON_GENDER(Res.string.person_gender);
}


enum class GenderType(override val label: StringResource) : OptionWithLabelStringResource {
    MALE(Res.string.male),
    FEMALE(Res.string.female),
    OTHER(Res.string.other);
}

interface OptionWithLabelStringResource {
    val label: StringResource
}
