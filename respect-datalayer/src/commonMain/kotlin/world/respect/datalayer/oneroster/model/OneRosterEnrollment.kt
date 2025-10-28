package world.respect.datalayer.oneroster.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.ExperimentalTime

/**
 * As per spec 6.1.14:
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_Enrollment
 */
@Suppress("unused")
@OptIn(ExperimentalTime::class)
class OneRosterEnrollment(
    override val sourcedId: String,
    override val status: OneRosterBaseStatusEnum = OneRosterBaseStatusEnum.ACTIVE,
    override val dateLastModified: InstantAsISO8601,
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