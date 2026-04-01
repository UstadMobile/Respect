package world.respect.datalayer.school.model

import kotlin.time.Clock
import kotlin.time.Instant

fun <T> findDifference(
    field: ChangeHistoryFieldEnum,
    oldVal: T?,
    newVal: T?,
    changes: MutableList<ChangeHistoryChange>,
    now: Instant = Clock.System.now(),
) {
    val oldString = oldVal?.toString()
    val newString = newVal?.toString()

    if (oldString != newString) {
        changes.add(
            ChangeHistoryChange(
                field = field,
                oldVal = oldString,
                newVal = newString ?: "",
                lastModified = now,
                stored = now
            )
        )
    }
}