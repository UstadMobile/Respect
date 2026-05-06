package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ChangeHistoryWithChanges(

    @Embedded
    val history: ChangeHistoryEntity,

    @Relation(
        parentColumn = ChangeHistoryEntity.PARENT_COLUMN,
        entityColumn = ChangeHistoryChangeEntity.CHILD_COLUMN
    )
    val changes: List<ChangeHistoryChangeEntity>
)