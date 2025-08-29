package world.respect.datalayer.oneroster.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * See OneRoster spec 6.1.5
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_Class
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class OneRosterClass(
    override val sourcedId: String,
    override val status: OneRosterBaseStatusEnum = OneRosterBaseStatusEnum.ACTIVE,
    @Serializable(with = InstantISO8601Serializer::class)
    override val dateLastModified: Instant,
    override val metadata: JsonObject? = null,
    val title: String,
    val location: String? = null,
) : OneRosterBase
{
    companion object{
        const val TABLE_ID = 23
    }
}