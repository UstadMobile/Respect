package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.random.Random
import kotlin.time.Clock

@Serializable
data class Clazz(
    val guid: String,
    val title: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    val description: String? = null,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
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
