package world.respect.datalayer.school.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock


@Serializable
data class Enrollment(
    val uid: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val metadata: JsonObject? = null,
    val classUid: String,
    val personUid: String,
    val role: EnrollmentRoleEnum,
    val beginDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val inviteCode: String? = null,
    val approvedByPersonUid: String? = null,
): ModelWithTimes {

    companion object {

        const val TABLE_ID = 6

    }
}
