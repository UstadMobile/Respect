package world.respect.shared.domain.report.ext

import world.respect.datalayer.db.shared.entities.ReportQueryResult
import world.respect.datalayer.school.model.report.StatementReportRow

fun ReportQueryResult.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)
