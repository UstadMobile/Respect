package world.respect.shared.domain.report.formatter

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import world.respect.shared.domain.report.model.ReportXAxis
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText

/**
 * Formatter for date values (handles different date groupings)
 */
class DateGraphFormatter(
    private val xAxisType: ReportXAxis
) : GraphFormatter<String> {

    override fun adjust(value: String): String {
        return value
    }

    override fun format(value: String): UiText {
        return try {
            when (xAxisType) {
                ReportXAxis.MONTH -> {
                    val date = LocalDate.parse(value)
                    StringUiText("${date.month} - ${date.year}")
                }

                ReportXAxis.YEAR -> StringUiText(value.toLocalDate().year.toString())
                else -> StringUiText(value)
            }
        } catch (e: Exception) {
            StringUiText(value)
        }
    }
}