package world.respect.datalayer.db.school.adapters

import androidx.room.Embedded
import androidx.room.Relation
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.AssignmentAssigneeRefEntity
import world.respect.datalayer.db.school.entities.AssignmentEntity
import world.respect.datalayer.db.school.entities.AssignmentLearningResourceRefEntity
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentAssigneeRef
import world.respect.datalayer.school.model.AssignmentLearningUnitRef

data class AssignmentEntities(
    @Embedded
    val assignment: AssignmentEntity,

    @Relation(
        parentColumn = "aeUidNum",
        entityColumn = "aarAeUidNum"
    )
    val assignees: List<AssignmentAssigneeRefEntity> = emptyList(),

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
        deadline = assignment.aeDeadline,
        lastModified = assignment.aeLastModified,
        stored = assignment.aeStored,
        assignees = assignees.map {
            AssignmentAssigneeRef(
                type = it.aarType,
                uid = it.aarAeAssigneeUid
            )
        },
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
            aeDeadline = deadline,
            aeLastModified = lastModified,
            aeStored = stored,
        ),
        assignees = assignees.map {
            AssignmentAssigneeRefEntity(
                aarAeUidNum = assignmentUidNum,
                aarType = it.type,
                aarAeAssigneeUid = it.uid,
                aarAeAssigneeUidNum = uidNumberMapper(it.uid),
            )
        },
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