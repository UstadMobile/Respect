package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.report.ReportOptions
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.datalayer.shared.serialization.InstantISO8601Serializer
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Report(
    val guid: String,
    val ownerGuid: String,
    val title: String,
    val reportOptions: ReportOptions,
    val reportIsTemplate: Boolean = false,
    val active: Boolean = true,
    @Serializable(with = InstantISO8601Serializer::class)
    override val lastModified: Instant = Clock.System.now(),
    @Serializable(with = InstantISO8601Serializer::class)
    override val stored: Instant = Clock.System.now(),
) : ModelWithTimes {

    companion object {
        const val TABLE_ID = 4
    }
}