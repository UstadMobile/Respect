package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import androidx.room.Relation
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.AssignmentEntity
import world.respect.datalayer.db.school.entities.AssignmentLearningResourceRefEntity
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef

data class AssignmentEntities(
    @Embedded
    val assignment: AssignmentEntity,

    @Relation(
        parentColumn = "aeUidNum",
        entityColumn = "alrrAeUidNum"
    )
    val learningUnits: List<AssignmentLearningResourceRefEntity> = emptyList(),
)

fun AssignmentEntities.toModel(): Assignment {
    return Assignment(
        uid = assignment.aeUid,
        title = assignment.aeTitle,
        description = assignment.aeDescription,
        classUid = assignment.aeClassUid,
        deadline = assignment.aeDeadline,
        lastModified = assignment.aeLastModified,
        stored = assignment.aeStored,
        learningUnits = learningUnits.map {
            AssignmentLearningUnitRef(
                learningUnitManifestUrl = it.alrrLearningUnitManifestUrl,
                appManifestUrl = it.alrrAppManifestUrl
            )
        },
    )
}

fun Assignment.toEntities(
    uidNumberMapper: UidNumberMapper
): AssignmentEntities {
    val assignmentUidNum = uidNumberMapper(uid)
    return AssignmentEntities(
        assignment = AssignmentEntity(
            aeUid = uid,
            aeUidNum = assignmentUidNum,
            aeTitle = title,
            aeDescription = description,
            aeClassUid = classUid,
            aeClassUidNum = uidNumberMapper(classUid),
            aeDeadline = deadline,
            aeLastModified = lastModified,
            aeStored = stored,
        ),
        learningUnits = learningUnits.map {
            AssignmentLearningResourceRefEntity(
                alrrAeUidNum = assignmentUidNum,
                alrrLearningUnitManifestUrl = it.learningUnitManifestUrl,
                alrrLearningUnitManifestUrlHash = uidNumberMapper(it.learningUnitManifestUrl.toString()),
                alrrAppManifestUrl = it.appManifestUrl,
            )
        }
    )
}