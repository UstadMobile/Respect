package world.respect.datalayer.school.model.report

import kotlinx.serialization.Serializable
import world.respect.lib.opds.model.LangMap

/**
 * Represents options selected by the user to generate a report. This is serialized into JSON
 * which is saved as to a string field on the Report entity.
 */
@Serializable
data class ReportOptions(
    val title: LangMap = LangMap.EMPTY,
    val xAxis: ReportXAxis = ReportXAxis.DAY,
    val period: ReportPeriod = ReportPeriodOption.LAST_WEEK.period,
    val series: List<ReportSeries> = emptyList(),
)