package world.respect.datalayer.db.school.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.db.realm.entities.IndicatorEntity
import world.respect.datalayer.db.realm.entities.ReportEntity
import world.respect.datalayer.school.model.report.ReportOptions
import world.respect.datalayer.respect.model.Indicator
import world.respect.datalayer.respect.model.RespectReport

// Extension functions for conversion
fun ReportEntity.toRespectReport(): RespectReport {
    return RespectReport(
        reportId = reportId,
        ownerGuid = ownerGuid,
        title = title,
        reportOptions = Json.decodeFromString(
            ReportOptions.serializer(), reportOptions.trim()
        ),
        reportIsTemplate = reportIsTemplate,
        active = active
    )
}

fun RespectReport.toReportEntity(): ReportEntity {
    return ReportEntity(
        reportId = reportId,
        ownerGuid = ownerGuid,
        title = title,
        reportOptions = Json.encodeToString(reportOptions),
        reportIsTemplate = reportIsTemplate,
        active = active
    )
}

fun IndicatorEntity.toIndicator(): Indicator {
    return Indicator(
        indicatorId = this.indicatorId,
        name = this.name,
        description = this.description,
        type = this.type,
        sql = this.sql
    )
}

fun Indicator.toIndicatorEntity(): IndicatorEntity {
    return IndicatorEntity(
        indicatorId = this.indicatorId,
        name = this.name,
        description = this.description,
        type = this.type,
        sql = this.sql
    )
}