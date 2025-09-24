package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.EnrollmentEntity
import world.respect.datalayer.school.model.Enrollment

/**
 * @property classUid as per EnrollmentEntity this needs to be retrieved from ClassEntity
 * @property personUid as per EnrollmentEntity this needs to be retrieved from PersonEntity
 */
data class EnrollmentEntities(
    @Embedded
    val enrollment: EnrollmentEntity,
    val classUid: String,
    val personUid: String,
)

fun EnrollmentEntities.toModel(): Enrollment {
    return Enrollment(
        uid = enrollment.eUid,
        status = enrollment.eStatus,
        lastModified = enrollment.eLastModified,
        stored = enrollment.eStored,
        metadata = enrollment.eMetadata,
        classUid = classUid,
        personUid = personUid,
        role = enrollment.eRole,
        beginDate = enrollment.eBeginDate,
        endDate = enrollment.eEndDate,
        inviteCode = enrollment.eInviteCode,
        approvedByPersonUid = enrollment.eApprovedByPersonUid,
    )
}

fun Enrollment.toEntities(
    uidNumberMapper: UidNumberMapper,
): EnrollmentEntities {
    val eUidNum = uidNumberMapper(uid)
    return EnrollmentEntities(
        enrollment = EnrollmentEntity(
            eUid = uid,
            eUidNum = eUidNum,
            eStatus = status,
            eLastModified = lastModified,
            eStored = stored,
            eMetadata = metadata,
            eClassUidNum = uidNumberMapper(classUid),
            ePersonUidNum = uidNumberMapper(personUid),
            eRole = role,
            eBeginDate = beginDate,
            eEndDate = endDate,
            eInviteCode = inviteCode,
            eApprovedByPersonUidNum = approvedByPersonUid?.let { uidNumberMapper(it) } ?: 0,
            eApprovedByPersonUid = approvedByPersonUid,
        ),
        classUid = classUid,
        personUid = personUid,
    )
}
