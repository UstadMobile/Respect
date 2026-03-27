package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.ChangeHistoryChangeEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryWithChanges
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.ChangeHistoryChange
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum
import world.respect.datalayer.school.model.ChangeHistoryTableEnum
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.findDifference
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Instant


data class ChangeHistoryEntities(
    val changeHistoryEntity: ChangeHistoryEntity,
    val changeEntities: List<ChangeHistoryChangeEntity>
)


fun ChangeHistoryWithChanges.toModel(): ChangeHistoryEntry {
    return ChangeHistoryEntry(
        guid = history.hGuid,
        table = history.hTable,
        whoGuid = history.hWhoGuid,
        tableGuid = history.hTableGuid,
        lastModified = history.hLastModified,
        stored = history.hStored,
        changes = changes.map { it.toModel() }
    )
}


fun ChangeHistoryChangeEntity.toModel(): ChangeHistoryChange {
    return ChangeHistoryChange(
        id = hcId,
        field = hcField,
        oldVal = hcOldVal,
        newVal = hcNewVal,
        synced = hcSynced,
        lastModified = hcLastModified,
        stored = hcStored
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
        hWhoGuid = whoGuid,
        hLastModified = lastModified,
        hWhoGuidHash = uidNumberMapper(whoGuid),
        hTableGuid = tableGuid
    )

    val changeEntities = changes.map { change ->
        ChangeHistoryChangeEntity(
            hcId = change.id ,
            hcHistoryGuidHash = guidHash,
            hcField = change.field,
            hcOldVal = change.oldVal,
            hcLastModified= change.lastModified,
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
    val now = Instant.fromEpochMilliseconds(timestamp)

    val changes = mutableListOf<ChangeHistoryChange>()


    findDifference(ChangeHistoryFieldEnum.CLASS_TITLE, old?.title, new.title,changes)
    findDifference(ChangeHistoryFieldEnum.CLASS_DESCRIPTION, old?.description, new.description,changes)
    findDifference(ChangeHistoryFieldEnum.CLASS_STATUS, old?.status, new.status,changes)

    findDifference(ChangeHistoryFieldEnum.CLASS_STATUS, old?.status, new.status, changes)

    if (changes.isEmpty()) return null

    return ChangeHistoryEntry(
        guid = primaryKeyGenerator.nextId(Clazz.TABLE_ID).toString(),
        table = ChangeHistoryTableEnum.CLASS,
        tableGuid = hTableGuid,
        whoGuid = whoGuid,
        changes = changes,
        lastModified = now,
        stored = now
    )
}


private fun roleChangeField(
    oldRole: EnrollmentRoleEnum?,
    newRole: EnrollmentRoleEnum
): ChangeHistoryFieldEnum? {
    return when (oldRole) {
        EnrollmentRoleEnum.PENDING_STUDENT if newRole == EnrollmentRoleEnum.STUDENT ->
            ChangeHistoryFieldEnum.JOIN_REQUEST_APPROVED
        EnrollmentRoleEnum.PENDING_TEACHER if newRole == EnrollmentRoleEnum.TEACHER ->
            ChangeHistoryFieldEnum.JOIN_REQUEST_APPROVED
        else -> null
    }
}

fun generateEnrollmentChanges(
    hGuid: String,
    old: Enrollment?,
    new: Enrollment,
    whoGuid: String,
    timestamp: Long,
    hTableGuid: String,
    personName: String?
): ChangeHistoryEntry? {
    val now = Instant.fromEpochMilliseconds(timestamp)

    val changes = mutableListOf<ChangeHistoryChange>()
    val name = personName ?: new.personUid

    if (old == null) {
        changes += ChangeHistoryChange(
            field = when (new.role) {
                EnrollmentRoleEnum.TEACHER -> ChangeHistoryFieldEnum.CLASS_TEACHER_ADDED
                EnrollmentRoleEnum.STUDENT -> ChangeHistoryFieldEnum.CLASS_STUDENT_ADDED
                else -> ChangeHistoryFieldEnum.CLASS_STUDENT_ADDED
            },
            oldVal = null,
            newVal = name,
            lastModified = now,
            stored = now
        )
    } else {

        if (new.status == StatusEnum.TO_BE_DELETED && old.status == StatusEnum.ACTIVE) {
            changes += ChangeHistoryChange(
                field = when (new.role) {
                    EnrollmentRoleEnum.TEACHER -> ChangeHistoryFieldEnum.CLASS_TEACHER_REMOVED
                    EnrollmentRoleEnum.STUDENT -> ChangeHistoryFieldEnum.CLASS_STUDENT_REMOVED
                    else -> ChangeHistoryFieldEnum.CLASS_STUDENT_REMOVED
                },
                oldVal = "",
                newVal = name,
                lastModified = now,
                stored = now
            )

            return ChangeHistoryEntry(
                guid = hGuid,
                table = ChangeHistoryTableEnum.CLASS,
                tableGuid = hTableGuid,
                whoGuid = whoGuid,
                changes = changes,
                lastModified = now,
                stored = now
            )
        }

        roleChangeField(old.role, new.role)?.let {
            changes += ChangeHistoryChange(
                field = it,
                oldVal = name,
                newVal = name,
                lastModified = now,
                stored = now
            )

            return ChangeHistoryEntry(
                guid = hGuid,
                table = ChangeHistoryTableEnum.CLASS,
                tableGuid = hTableGuid,
                whoGuid = whoGuid,
                changes = changes,
                lastModified = now,
                stored = now
            )
        }

        findDifference(ChangeHistoryFieldEnum.ENROLLMENT_ROLE, old.role, new.role, changes,now)
        findDifference(ChangeHistoryFieldEnum.ENROLLMENT_BEGIN_DATE, old.beginDate, new.beginDate, changes)
        findDifference(ChangeHistoryFieldEnum.ENROLLMENT_END_DATE, old.endDate, new.endDate, changes)
        findDifference(ChangeHistoryFieldEnum.ENROLLMENT_STATUS, old.status, new.status, changes)
    }

    if (changes.isEmpty()) return null

    return ChangeHistoryEntry(
        guid = hGuid,
        table = ChangeHistoryTableEnum.ENROLLMENT,
        tableGuid = hTableGuid,
        whoGuid = whoGuid,
        changes = changes,
        lastModified = now,
        stored = now
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
    val now = Instant.fromEpochMilliseconds(timestamp)
    val changes = mutableListOf<ChangeHistoryChange>()

    findDifference(ChangeHistoryFieldEnum.PERSON_GIVEN_NAME, old?.fullName(), new.fullName(), changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_USERNAME, old?.username, new.username, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_GENDER, old?.gender, new.gender, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_EMAIL, old?.email, new.email, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_PHONE_NUMBER, old?.phoneNumber, new.phoneNumber, changes)
    findDifference(ChangeHistoryFieldEnum.PERSON_DATE_OF_BIRTH, old?.dateOfBirth, new.dateOfBirth, changes)

    if (changes.isEmpty()) return null

    return ChangeHistoryEntry(
        guid = primaryKeyGenerator.nextId(ChangeHistoryEntry.TABLE_ID).toString(),
        table = ChangeHistoryTableEnum.PERSON,
        whoGuid = whoGuid,
        changes = changes,
        tableGuid = hTableGuid,
        lastModified = now,
        stored = now
    )
}