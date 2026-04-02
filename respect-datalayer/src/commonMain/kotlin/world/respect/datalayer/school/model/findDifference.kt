package world.respect.datalayer.school.model

fun <T> findDifference(
    field: ChangeHistoryFieldEnum,
    oldVal: T?,
    newVal: T?,
    changes: MutableList<ChangeHistoryChange>
) {
    val oldString = oldVal?.toString()
    val newString = newVal?.toString()

    if (oldString != newString) {
        changes.add(
            ChangeHistoryChange(
                field = field,
                oldVal = oldString,
                newVal = newString ?: ""
            )
        )
    }
}