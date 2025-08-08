package world.respect.shared.domain.report.model

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

/** Enum representing different comparison types */
enum class Comparisons(
    override val label: StringResource,
    val symbol: String
) : OptionWithLabelStringResource {
    EQUALS(Res.string.equals, "="),
    NOT_EQUALS(Res.string.not_equals, "!="),
    GREATER(Res.string.greater, ">"),
    LESSER(Res.string.lesser, "<"),
    GREATER_OR_EQUAL(Res.string.greater_or_equal, ">="),
    LESSER_OR_EQUAL(Res.string.lesser_or_equal, "<="),
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

