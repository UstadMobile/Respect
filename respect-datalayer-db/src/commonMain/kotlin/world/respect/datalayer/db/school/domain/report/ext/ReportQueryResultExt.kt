package world.respect.datalayer.db.school.domain.report.ext

import world.respect.datalayer.db.shared.entities.ReportQueryResultEntity
import world.respect.lib.xapi.extensions.reportoptions.StatementReportRow

fun ReportQueryResultEntity.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)

fun List<ReportQueryResultEntity>.age(sinceTimestamp: Long): Int {
    if (isEmpty()) return 0
    val lastModified = maxOf { it.rqrLastModified }
    return ((sinceTimestamp - lastModified) / 1000).toInt().coerceAtLeast(0)
}
