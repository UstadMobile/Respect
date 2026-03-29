package world.respect.datalayer.db.school

import world.respect.datalayer.ChangeHistoryProvider
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.model.ChangeHistoryTableEnum

class ChangeHistoryProviderDb(
    private val schoolDb: RespectSchoolDatabase,
) : ChangeHistoryProvider {

    override suspend fun getChangeHistoryEntries(
        tableId: ChangeHistoryTableEnum,
        uids: List<String>
    ): List<ChangeHistoryEntry> {

        return schoolDb.getChangeHistoryDao().getByTableAndUids(
            tableEnum = tableId,
            uids = uids
        ).map { it.toModel() }
    }
}

