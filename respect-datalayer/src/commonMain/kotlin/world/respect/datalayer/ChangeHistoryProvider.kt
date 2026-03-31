package world.respect.datalayer

import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.model.ChangeHistoryTableEnum

interface ChangeHistoryProvider {
    suspend fun getPendingChangeHistoryEntries(
        tableId: ChangeHistoryTableEnum,
        uids: List<String>
    ): List<ChangeHistoryEntry>
}

