package world.respect.shared.domain.report.model

import world.respect.datalayer.db.school.domain.report.query.RunReportUseCase
import world.respect.shared.domain.report.formatter.GraphFormatter

data class RunReportResultAndFormatters(
    val reportResult: RunReportUseCase.RunReportResult,
    val xAxisFormatter: GraphFormatter<String>?,
    val yAxisFormatter: GraphFormatter<Double>?
)