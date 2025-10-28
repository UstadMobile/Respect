package world.respect.datalayer.oneroster.model

import com.eygraber.uri.Uri
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * As per OneRoster spec 6.1.22:
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_Role
 *
 * "Indicates if this role is the primary or secondary role for that org. There MUST be one, and only one, primary role for each org."
 */
@Serializable
data class OneRosterRole(
    val roleTypeEnum: OneRosterRoleTypeEnum = OneRosterRoleTypeEnum.PRIMARY,
    val role: OneRosterRoleEnum,
    val org: OneRosterOrgGUIDRef,
    val userProfile: Uri? = null,
    val beginDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
