package world.respect.shared.domain.report.ext

import world.respect.datalayer.db.shared.entities.ReportQueryResult
import world.respect.shared.domain.report.model.StatementReportRow

fun ReportQueryResult.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)
