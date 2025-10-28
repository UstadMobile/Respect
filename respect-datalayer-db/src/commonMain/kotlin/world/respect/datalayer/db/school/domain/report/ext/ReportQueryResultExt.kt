package world.respect.datalayer.db.school.domain.report.ext

import world.respect.datalayer.db.school.entities.ReportQueryResult
import world.respect.datalayer.school.model.report.StatementReportRow

fun ReportQueryResult.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)
