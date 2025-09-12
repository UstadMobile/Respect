package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import world.respect.datalayer.db.school.entities.EnrollmentEntity
import world.respect.datalayer.school.model.Enrollment
import world.respect.libxxhash.XXStringHasher

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
        classUid = classUid,
        personUid = personUid,
        role = enrollment.eRole,
        beginDate = enrollment.eBeginDate,
        endDate = enrollment.eEndDate,
    )
}

fun Enrollment.toEntities(
    xxStringHasher: XXStringHasher
): EnrollmentEntities {
    val eUidNum = xxStringHasher.hash(uid)
    return EnrollmentEntities(
        enrollment = EnrollmentEntity(
            eUid = uid,
            eUidNum = eUidNum,
            eStatus = status,
            eLastModified = lastModified,
            eStored = stored,
            eClassUidNum = xxStringHasher.hash(classUid),
            ePersonUidNum = xxStringHasher.hash(personUid),
            eRole = role,
            eBeginDate = beginDate,
            eEndDate = endDate,
        ),
        classUid = classUid,
        personUid = personUid,
    )
}
