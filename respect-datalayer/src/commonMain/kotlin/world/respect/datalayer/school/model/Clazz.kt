package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Clazz(
    val guid: String,
    val title: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    val description: String? = null,
    @Serializable(with = InstantISO8601Serializer::class)
    override val lastModified: Instant = Clock.System.now(),
    @Serializable(with = InstantISO8601Serializer::class)
    override val stored: Instant = Clock.System.now(),
    val teacherInviteCode: String? = Random.nextInt(
        DEFAULT_INVITE_CODE_MAX
    ).toString().padStart(DEFAULT_INVITE_CODE_LEN, '0'),
    val studentInviteCode: String? = Random.nextInt(
        DEFAULT_INVITE_CODE_MAX
    ).toString().padStart(DEFAULT_INVITE_CODE_LEN, '0'),
): ModelWithTimes {

    companion object {

        const val TABLE_ID = 8

        const val DEFAULT_INVITE_CODE_MAX = 100_000
        const val DEFAULT_INVITE_CODE_LEN = 6
    }

}
