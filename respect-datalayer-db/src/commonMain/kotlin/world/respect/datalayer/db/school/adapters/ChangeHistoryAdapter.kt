package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ChangeHistoryChangeEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryWithChanges
import world.respect.datalayer.school.model.ChangeHistoryChange
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum
import world.respect.datalayer.school.model.ChangeHistoryTableEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.findDifference


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
fun generatePersonChanges(
    hGuid: String,
    old: Person?,
    new: Person,
    whoGuid: String,
    timestamp: Long
): ChangeHistoryEntry? {

    val changes = mutableListOf<ChangeHistoryChange>()

    findDifference(ChangeHistoryFieldEnum.PERSON_GIVEN_NAME, old?.givenName, new.givenName, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_FAMILY_NAME, old?.familyName, new.familyName, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_MIDDLE_NAME, old?.middleName, new.middleName, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_USERNAME, old?.username, new.username, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_GENDER, old?.gender, new.gender, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_EMAIL, old?.email, new.email, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_PHONE_NUMBER, old?.phoneNumber, new.phoneNumber, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_DATE_OF_BIRTH, old?.dateOfBirth, new.dateOfBirth, changes)

    if (changes.isEmpty()) return null

    return ChangeHistoryEntry(
        guid = hGuid,
        table = ChangeHistoryTableEnum.PERSON,
        timestamp = timestamp,
        whoGuid = whoGuid,
        changes = changes
    )
}