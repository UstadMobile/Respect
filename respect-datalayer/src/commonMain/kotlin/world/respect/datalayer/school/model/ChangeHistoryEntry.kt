package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class ChangeHistoryEntry(
    val guid: String,
    val tableGuid: String,
    val table: ChangeHistoryTableEnum,
    val whoGuid: String,
    val changes: List<ChangeHistoryChange>,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
): ModelWithTimes  {

    companion object {

        const val TABLE_ID = 31

    }
}


@Serializable
data class ChangeHistoryEntryWithWhoDid(
    val person: Person,
    val changeHistoryEntry: List<ChangeHistoryEntry>
)

@Serializable
data class ChangeHistoryChange(
    val id : Long =0,
    val field: ChangeHistoryFieldEnum,
    val newVal: String,
    val oldVal: String?,
    val synced : Boolean = false,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
): ModelWithTimes