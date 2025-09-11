package world.respect.datalayer.school.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.Clock
import kotlin.time.Instant


@Serializable
data class Enrollment(
    val uid: String,
    val status: StatusEnum,
    @Serializable(with = InstantISO8601Serializer::class)
    override val lastModified: Instant = Clock.System.now(),
    @Serializable(with = InstantISO8601Serializer::class)
    override val stored: Instant = Clock.System.now(),
    val classUid: String,
    val personUid: String,
    val role: EnrollmentRoleEnum,
    val beginDate: LocalDate? = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()
    ).date,
    val endDate: LocalDate? = null,
): ModelWithTimes
