package world.respect.datalayer.oneroster.model

import kotlinx.serialization.Serializable

/**
 * As per OneRoster spec 6.3.12:
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Enumerated_RoleTypeEnum
 *
 */
@Serializable
enum class OneRosterRoleTypeEnum(val value: String) {

    PRIMARY("primary"), SECONDARY("secondary")
}