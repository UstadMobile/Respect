package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601

/**
 * Represents a grant of permissions to a given role in the school.
 *
 * @property permissions Flags for permissions granted - see PermissionFlags
 */
@Serializable
data class SchoolPermissionGrant(
    val uid: String,
    val statusEnum: StatusEnum,
    val toRole: PersonRoleEnum,
    val permissions: Long,
    override val stored: InstantAsISO8601,
    override val lastModified: InstantAsISO8601,
) : ModelWithTimes
