package world.respect.datalayer

import world.respect.datalayer.school.model.ChangeHistoryEntry

interface ChangeHistoryMarkSentToServer {
    suspend fun markSentToServer(
        changeHistoryEntry: List<ChangeHistoryEntry>
    )
}