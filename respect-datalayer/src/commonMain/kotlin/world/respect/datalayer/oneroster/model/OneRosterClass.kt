package world.respect.datalayer.oneroster.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.ExperimentalTime

/**
 * See OneRoster spec 6.1.5
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_Class
 */
@Suppress("unused")
@OptIn(ExperimentalTime::class)
@Serializable
data class OneRosterClass(
    override val sourcedId: String,
    override val status: OneRosterBaseStatusEnum = OneRosterBaseStatusEnum.ACTIVE,
    override val dateLastModified: InstantAsISO8601,
    override val metadata: JsonObject? = null,
    val title: String,
    val location: String? = null,
) : OneRosterBase
{
    companion object{
        const val TABLE_ID = 23
    }
}