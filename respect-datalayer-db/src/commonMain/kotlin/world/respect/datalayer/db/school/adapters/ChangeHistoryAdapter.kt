package world.respect.datalayer.db.school.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity
import world.respect.datalayer.school.model.ChangeHistoryEntry


data class ChangeHistoryEntities(
    val changeHistoryEntity: ChangeHistoryEntity
)

fun ChangeHistoryEntities.toModel(): ChangeHistoryEntry {
    return ChangeHistoryEntry(
        guid = changeHistoryEntity.hGuid,
        table = changeHistoryEntity.hTable,
        timestamp = changeHistoryEntity.hTimestamp,
        whoGuid = changeHistoryEntity.hWhoGuid,
        changes = Json.decodeFromString(changeHistoryEntity.hChanges)
    )
}

fun ChangeHistoryEntry.toEntities(
    uidNumberMapper: UidNumberMapper
): ChangeHistoryEntities {

    val hGuidHash = uidNumberMapper(guid)

    return ChangeHistoryEntities(
        changeHistoryEntity = ChangeHistoryEntity(
            hGuid = guid,
            hGuidHash = hGuidHash,
            hTable = table,
            hTimestamp = timestamp,
            hWhoGuid = whoGuid,
            hWhoGuidHash = uidNumberMapper(whoGuid),
            hChanges = Json.encodeToString(changes)
        )
    )
}