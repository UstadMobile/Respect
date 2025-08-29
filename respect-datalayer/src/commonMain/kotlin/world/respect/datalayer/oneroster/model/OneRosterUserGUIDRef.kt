package world.respect.datalayer.oneroster.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable

/**
 * As per Spec 6.1.31
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_UserGUIDRef
 */
@Serializable
class OneRosterUserGUIDRef(
    override val href: Uri,
    override val sourcedId: String,
    val type: UserGUIDRefTypeEnum = UserGUIDRefTypeEnum.USER,
) : OneRosterGUIDRef{

    enum class UserGUIDRefTypeEnum(val value: String) {
        USER("user")
    }
}