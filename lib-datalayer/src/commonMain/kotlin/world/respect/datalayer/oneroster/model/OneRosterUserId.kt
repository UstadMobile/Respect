package world.respect.datalayer.oneroster.model

import kotlinx.serialization.Serializable

/**
 * See OneRoster Spec 6.1.32:
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#FigDataClass_DataModel_UserId
 *
 */
@Serializable
data class OneRosterUserId(
    val type: String,
    val identifier: String,
)
