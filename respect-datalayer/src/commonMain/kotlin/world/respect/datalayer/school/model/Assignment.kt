package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

@Serializable
data class Assignment(
    val uid: String,
    val title: String,
    val description: String,
    val deadline: InstantAsISO8601,
    val assignees: List<AssignmentAssigneeRef> = emptyList(),
    val learningUnits: List<AssignmentLearningResourceRef> = emptyList(),
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
) : ModelWithTimes

