package world.respect.datalayer.db.school.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ReportEntity
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.school.model.report.ReportOptions
import kotlin.time.Instant


data class ReportEntities(
    val reportEntity: ReportEntity
)

// Extension functions for conversion
fun ReportEntities.toModel(): Report {
    return Report(
        guid = reportEntity.rGuid,
        ownerGuid = reportEntity.rOwnerGuid,
        title = reportEntity.rTitle,
        reportOptions = Json.decodeFromString(
            ReportOptions.serializer(), reportEntity.rOptions.trim()
        ),
        reportIsTemplate = reportEntity.rIsTemplate,
        active = reportEntity.rActive,
        stored = Instant.fromEpochMilliseconds(reportEntity.rStored),
        lastModified = Instant.fromEpochMilliseconds(reportEntity.rLastModified),
    )
}

fun Report.toEntities(
    uidNumberMapper: UidNumberMapper
): ReportEntities {
    val rGuidHash = uidNumberMapper(guid)
    return ReportEntities(
        reportEntity = ReportEntity(
            rGuid = guid,
            rGuidHash = rGuidHash,
            rOwnerGuid = ownerGuid,
            rTitle = title,
            rOptions = Json.encodeToString(reportOptions),
            rIsTemplate = reportIsTemplate,
            rActive = active,
            rLastModified = lastModified.toEpochMilliseconds(),
            rStored = stored.toEpochMilliseconds(),
        )
    )
}