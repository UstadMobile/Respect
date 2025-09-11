package world.respect.datalayer.school.model.report

import kotlinx.serialization.Serializable

/** Enum representing different comparison types */
enum class Comparisons(val symbol: String) {
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER(">"),
    LESSER("<"),
    GREATER_OR_EQUAL(">="),
    LESSER_OR_EQUAL("<="),
}

/** Sealed class representing different types of report filters */
@Serializable
sealed class ReportConditionFilterOptions(
    val comparisonTypes: List<Comparisons>,
) {
    @Serializable
    class GenderConditionFilter : ReportConditionFilterOptions(
        comparisonTypes = listOf(Comparisons.EQUALS, Comparisons.NOT_EQUALS)
    )

    @Serializable
    class AgeConditionFilter : ReportConditionFilterOptions(
        comparisonTypes = listOf(
            Comparisons.EQUALS,
            Comparisons.NOT_EQUALS,
            Comparisons.GREATER,
            Comparisons.LESSER,
            Comparisons.GREATER_OR_EQUAL,
            Comparisons.LESSER_OR_EQUAL,
        )
    )
}

@Serializable
data class ReportFilter(
    var reportFilterUid: Int = 0,
    var reportFilterSeriesUid: Int = 0,
    var reportFilterField: FilterType? = null,
    var reportFilterCondition: Comparisons? = null,
    var reportFilterValue: String? = ""
)