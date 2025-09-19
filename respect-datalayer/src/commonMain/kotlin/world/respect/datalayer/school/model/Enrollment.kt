package world.respect.datalayer.school.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.Clock
import kotlin.time.Instant


@Serializable
data class Enrollment(
    val uid: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    @Serializable(with = InstantISO8601Serializer::class)
    override val lastModified: Instant = Clock.System.now(),
    @Serializable(with = InstantISO8601Serializer::class)
    override val stored: Instant = Clock.System.now(),
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
