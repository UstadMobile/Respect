package world.respect.datalayer.school.model.report

import kotlinx.serialization.Serializable

@Serializable
data class StatementReportRow(
    var yAxis: Double = 0.toDouble(),
    var xAxis: String = "",
    var subgroup: String = "",
)
