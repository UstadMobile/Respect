package world.respect.datalayer.oneroster.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import world.respect.lib.serializers.UriStringSerializer

/**
 * AS per spec 6.1.19
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_OrgGUIDRef
 */
@Serializable
data class OneRosterOrgGUIDRef(
    val type: OrgGUIDRefTypeEnum = OrgGUIDRefTypeEnum.ORG,
    @Serializable(with = UriStringSerializer::class)
    override val href: Uri,
    override val sourcedId: String,
): OneRosterGUIDRef {

    enum class OrgGUIDRefTypeEnum(val value: String) {
        ORG("org")
    }

}