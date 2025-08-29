package world.respect.datalayer.respect.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.report.ReportOptions

@Serializable
data class RespectReport(
    val reportId: String,
    val ownerGuid: String,
    val title: String,
    val reportOptions: ReportOptions,
    val reportIsTemplate: Boolean = false,
    val active: Boolean = true
) {
    companion object {
        const val TABLE_ID = 4
    }
}

