package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ChangeHistoryChangeEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryWithChanges
import world.respect.datalayer.school.model.ChangeHistoryChange
import world.respect.datalayer.school.model.ChangeHistoryEntry


data class ChangeHistoryEntities(
    val changeHistoryEntity: ChangeHistoryEntity,
    val changeEntities: List<ChangeHistoryChangeEntity>
)


fun ChangeHistoryWithChanges.toModel(): ChangeHistoryEntry {
    return ChangeHistoryEntry(
        guid = history.hGuid,
        table = history.hTable,
        timestamp = history.hTimestamp,
        whoGuid = history.hWhoGuid,
        changes = changes.map { it.toModel() }
    )
}


fun ChangeHistoryChangeEntity.toModel(): ChangeHistoryChange {
    return ChangeHistoryChange(
        field = hcField,
        oldVal = hcOldVal,
        newVal = hcNewVal
    )
}

fun ChangeHistoryEntry.toEntities(
    uidNumberMapper: UidNumberMapper
): ChangeHistoryEntities {

    val guidHash = uidNumberMapper(guid)

    val historyEntity = ChangeHistoryEntity(
        hGuid = guid,
        hGuidHash = guidHash,
        hTable = table,
        hTimestamp = timestamp,
        hWhoGuid = whoGuid,
        hWhoGuidHash = uidNumberMapper(whoGuid),
        hChanges = ""
    )

    val changeEntities = changes.map { change ->
        ChangeHistoryChangeEntity(
            hcHistoryGuidHash = guidHash,
            hcField = change.field,
            hcOldVal = change.oldVal,
            hcNewVal = change.newVal
        )
    }

    return ChangeHistoryEntities(
        changeHistoryEntity = historyEntity,
        changeEntities = changeEntities
    )
}