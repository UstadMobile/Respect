package world.respect.datalayer.realm.model.report

import kotlinx.serialization.Serializable

@Serializable
data class StatementReportRow(
    var yAxis: Double = 0.toDouble(),
    var xAxis: String = "",
    var subgroup: String = "",
)
