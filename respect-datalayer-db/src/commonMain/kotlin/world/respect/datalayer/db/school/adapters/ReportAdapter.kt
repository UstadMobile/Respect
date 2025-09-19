package world.respect.datalayer.db.school.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.db.school.entities.ReportEntity
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.school.model.report.ReportOptions
import world.respect.libxxhash.XXStringHasher
import kotlin.time.Instant

// Extension functions for conversion
fun ReportEntity.toRespectReport(): Report {
    return Report(
        guid = rGuid,
        ownerGuid = rOwnerGuid,
        title = rTitle,
        reportOptions = Json.decodeFromString(
            ReportOptions.serializer(), rOptions.trim()
        ),
        reportIsTemplate = rIsTemplate,
        active = rActive,
        stored = Instant.fromEpochMilliseconds(rStored),
        lastModified = Instant.fromEpochMilliseconds(rLastModified),
    )
}

fun Report.toReportEntity(
    hasher: XXStringHasher,
): ReportEntity {
    return ReportEntity(
        rGuid = guid,
        rGuidHash = hasher.hash(guid),
        rOwnerGuid = ownerGuid,
        rTitle = title,
        rOptions = Json.encodeToString(reportOptions),
        rIsTemplate = reportIsTemplate,
        rActive = active,
        rLastModified = lastModified.toEpochMilliseconds(),
        rStored = stored.toEpochMilliseconds(),
    )
}