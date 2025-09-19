package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Indicator(
    val indicatorId: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val sql: String = "",
    @Serializable(with = InstantISO8601Serializer::class)
    override val lastModified: Instant = Clock.System.now(),
    @Serializable(with = InstantISO8601Serializer::class)
    override val stored: Instant = Clock.System.now(),
) : ModelWithTimes {
    companion object {
        const val TABLE_ID = 5
    }
}