package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ChangeHistoryChangeEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryWithChanges
import world.respect.datalayer.school.model.ChangeHistoryChange
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum
import world.respect.datalayer.school.model.ChangeHistoryTableEnum
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.findDifference
import world.respect.lib.primarykeygen.PrimaryKeyGenerator


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
        tableGuid = history.hTableGuid,
        changes = changes.map { it.toModel() }
    )
}


fun ChangeHistoryChangeEntity.toModel(): ChangeHistoryChange {
    return ChangeHistoryChange(
        id = hcId,
        field = hcField,
        oldVal = hcOldVal,
        newVal = hcNewVal,
        synced = hcSynced
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
        hTableGuid = tableGuid
    )

    val changeEntities = changes.map { change ->
        ChangeHistoryChangeEntity(
            hcId = change.id ,
            hcHistoryGuidHash = guidHash,
            hcField = change.field,
            hcOldVal = change.oldVal,
            hcNewVal = change.newVal,
            hcSynced = change.synced
        )
    }

    return ChangeHistoryEntities(
        changeHistoryEntity = historyEntity,
        changeEntities = changeEntities
    )
}
fun generateClassChanges(
    primaryKeyGenerator: PrimaryKeyGenerator,
    old: Clazz?,
    new: Clazz,
    whoGuid: String,
    timestamp: Long,
    hTableGuid: String
): ChangeHistoryEntry? {

    val changes = mutableListOf<ChangeHistoryChange>()


    findDifference(ChangeHistoryFieldEnum.CLASS_TITLE, old?.title, new.title,changes)
    findDifference(ChangeHistoryFieldEnum.CLASS_DESCRIPTION, old?.description, new.description,changes)
    findDifference(ChangeHistoryFieldEnum.CLASS_STATUS, old?.status, new.status,changes)

    if (changes.isEmpty()) return null

    return ChangeHistoryEntry(
        guid = primaryKeyGenerator.nextId(Clazz.TABLE_ID).toString(),
        table = ChangeHistoryTableEnum.CLASS,
        tableGuid = hTableGuid,
        whoGuid = whoGuid,
        timestamp = timestamp,
        changes = changes
    )
}


fun generateEnrollmentChanges(
    hGuid: String,
    old: Enrollment?,
    new: Enrollment,
    whoGuid: String,
    timestamp: Long,
    hTableGuid: String
): ChangeHistoryEntry? {

    val changes = mutableListOf<ChangeHistoryChange>()



    findDifference(ChangeHistoryFieldEnum.ENROLLMENT_ROLE, old?.role, new.role,changes)
    findDifference(ChangeHistoryFieldEnum.ENROLLMENT_BEGIN_DATE, old?.beginDate, new.beginDate,changes)
    findDifference(ChangeHistoryFieldEnum.ENROLLMENT_END_DATE, old?.endDate, new.endDate,changes)
    findDifference(ChangeHistoryFieldEnum.ENROLLMENT_STATUS, old?.status, new.status,changes)

    if (changes.isEmpty()) return null

    return ChangeHistoryEntry(
        guid = hGuid,
        table = ChangeHistoryTableEnum.ENROLLMENT,
        tableGuid = hTableGuid,
        whoGuid = whoGuid,
        timestamp = timestamp,
        changes = changes
    )
}

fun generatePersonChanges(
    primaryKeyGenerator: PrimaryKeyGenerator,
    old: Person?,
    new: Person,
    whoGuid: String,
    timestamp: Long,
    hTableGuid: String
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
        guid = primaryKeyGenerator.nextId(ChangeHistoryEntry.TABLE_ID).toString(),
        table = ChangeHistoryTableEnum.PERSON,
        timestamp = timestamp,
        whoGuid = whoGuid,
        changes = changes,
        tableGuid = hTableGuid
    )
}