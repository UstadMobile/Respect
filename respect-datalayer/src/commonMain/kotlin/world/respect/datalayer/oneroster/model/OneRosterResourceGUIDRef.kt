package world.respect.datalayer.oneroster.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.serialization.UriStringSerializer

/**
 * As per OneRoster spec: 6.1.21
 *
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_ResourceGUIDRef
 *
 */
@Serializable
data class OneRosterResourceGUIDRef(
    val type: ResourceGUIDRefTypeEnum = ResourceGUIDRefTypeEnum.RESOURCE,
    @Serializable(with = UriStringSerializer::class)
    val href: Uri,
    val sourcedId: String,
) {
    enum class ResourceGUIDRefTypeEnum(val value: String) {
        RESOURCE("resource")
    }
}
