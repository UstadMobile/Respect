package world.respect.shared.domain.report.model

import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import world.respect.datalayer.respect.model.Indicator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

@Serializable
data class ReportSeries(

    val reportSeriesUid: Int = 0,

    val reportSeriesTitle: String = "Series 1",

    val reportSeriesYAxis: Indicator = DefaultIndicators.list.first(),

    val reportSeriesVisualType: ReportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,

    val reportSeriesSubGroup: ReportXAxis? = ReportXAxis.DAY,

    val reportSeriesFilters: List<ReportFilter>? = null,

    )

enum class YAxisTypes(
    override val label: StringResource,
) : OptionWithLabelStringResource {
    COUNT(Res.string.count), DURATION(Res.string.duration), PERCENTAGE(Res.string.percentage)
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(override val label: StringResource) :
    OptionWithLabelStringResource {
    BAR_CHART(Res.string.bar_chart),
    LINE_GRAPH(Res.string.line_chart);
}

/**
 * Enum representing different X-axis or sub-group options for report series
 * Organized by: Time Dimensions, Educational Dimensions, Demographic Dimensions, Technical Dimensions
 */
enum class ReportXAxis(
    override val label: StringResource,
    val personJoinRequired: Boolean = false,
    val datePeriod: DatePeriod? = null,
) : OptionWithLabelStringResource {
    DAY(Res.string.day, datePeriod = DatePeriod(days = 1)),
    WEEK(Res.string.weekly, datePeriod = DatePeriod(days = 7)),
    MONTH(Res.string.monthly, datePeriod = DatePeriod(months = 1)),
    QUARTER(Res.string.quarterly, datePeriod = DatePeriod(months = 3)),
    YEAR(Res.string.yearly, datePeriod = DatePeriod(years = 1)),
    TIME_OF_DAY(Res.string.time_of_day),
    CLASS(Res.string.class_name),
    SUBJECT(Res.string.subject),
    SCHOOL(Res.string.school),
    ASSESSMENT_TYPE(Res.string.assessment_type),
    GRADE_LEVEL(Res.string.grade_level),
    GENDER(Res.string.gender, personJoinRequired = true),
    AGE_GROUP(Res.string.age_group, personJoinRequired = true),
    REGION(Res.string.region),
    LANGUAGE(Res.string.language, personJoinRequired = true),
    USER_ROLE(Res.string.user_role, personJoinRequired = true),
    ACTIVITY_VERB(Res.string.activity_verb),
    APPLICATION(Res.string.application),
    DEVICE_TYPE(Res.string.device_type),
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
