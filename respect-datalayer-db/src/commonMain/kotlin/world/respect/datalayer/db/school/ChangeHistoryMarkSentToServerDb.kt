package world.respect.datalayer.db.school

import world.respect.datalayer.ChangeHistoryMarkSentToServer
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.model.ChangeHistoryEntry

class ChangeHistoryMarkSentToServerDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper
) : ChangeHistoryMarkSentToServer {
    override suspend fun markSentToServer(changeHistoryEntry: List<ChangeHistoryEntry>) {

        if (changeHistoryEntry.isEmpty()) return

        val historyGuidHashes = changeHistoryEntry
            .map { uidNumberMapper(it.guid) }

        if (historyGuidHashes.isEmpty()) return

        schoolDb.getChangeHistoryDao().markByHistoryGuids(historyGuidHashes)    }


}