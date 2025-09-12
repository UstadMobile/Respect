package world.respect.datalayer.oneroster.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * As per spec 6.1.14:
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_Enrollment
 */
@OptIn(ExperimentalTime::class)
class OneRosterEnrollment(
    override val sourcedId: String,
    override val status: OneRosterBaseStatusEnum = OneRosterBaseStatusEnum.ACTIVE,
    @Serializable(with = InstantISO8601Serializer::class)
    override val dateLastModified: Instant,
    override val metadata: JsonObject? = null,
    val user: OneRosterUserGUIDRef,
    @SerialName("class")
    val clazz: OneRosterClassGUIDRef,
    val role: OneRosterRoleEnum,
    val primary: Boolean = true,
    val beginDate: LocalDate? = null,
    val endDate: LocalDate? = null,
): OneRosterBase {
}