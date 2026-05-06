package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.ChangeHistoryEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.ChangeHistoryDataSource
import world.respect.datalayer.school.ChangeHistoryLocal
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class ChangeHistoryDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper
    ) : ChangeHistoryLocal {



    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<List<ChangeHistoryEntry>> {
        val result = schoolDb.getChangeHistoryDao().findByGuid(guid)
        return if (!result.isNullOrEmpty()) {
            DataReadyState(result.map { it.toModel() })
        } else {
            NoDataLoadedState.notFound()
        }
    }

    override fun findByGuidAsFlow(
        guid: String
    ): Flow<DataLoadState<List<ChangeHistoryEntry>>> {
        return schoolDb.getChangeHistoryDao()
            .findByGuidAsFlow(guid)
            .map { list ->
                if (list.isNotEmpty()) {
                    DataReadyState(list.map { it.toModel() })
                } else {
                    NoDataLoadedState.notFound()
                }
            }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: ChangeHistoryDataSource.GetListParams
    ): DataLoadState<List<ChangeHistoryEntry>> {
        val result = schoolDb.getChangeHistoryDao().findByGuid(params.common.guid?:"")
        return if (!result.isNullOrEmpty()) {
            DataReadyState(result.map { it.toModel() })
        } else {
            NoDataLoadedState.notFound()
        }
    }

    override fun listAsPagingSource(
        dataLoadParams: DataLoadParams,
        getListParams: ChangeHistoryDataSource.GetListParams
    ):
            IPagingSourceFactory<Int, ChangeHistoryEntry> {

        return IPagingSourceFactory {
            schoolDb.getChangeHistoryDao()
                .listAsPagingSource().map {
                    it.toModel()
                }
        }
    }

    override suspend fun markSentToServer(
        changeHistoryEntries: List<ChangeHistoryEntry>
    ) {
        if (changeHistoryEntries.isEmpty()) return

        val historyGuidHashes = changeHistoryEntries
            .map { uidNumberMapper(it.guid) }

        if (historyGuidHashes.isEmpty()) return

        schoolDb.getChangeHistoryDao().markByHistoryGuids(historyGuidHashes)
    }

    override suspend fun store(list: List<ChangeHistoryEntry>) {
        if(list.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach {
                    val entities = it.toEntities(uidNumberMapper)
                    schoolDb.getChangeHistoryDao().insertHistoryWithChanges(
                        history = entities.changeHistoryEntity,
                        changes = entities.changeEntities
                    )
                }
            }
        }
    }

    override suspend fun updateLocal(
        list: List<ChangeHistoryEntry>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()

                val toUpdate: List<ChangeHistoryEntities> = list
                    .filter {
                        forceOverwrite || (
                                schoolDb.getChangeHistoryDao().getLastModifiedByUidNum(
                                    uidNum = uidNumberMapper(it.guid)
                                ) ?: 0L
                                ) < it.lastModified.toEpochMilliseconds()
                    }
                    .map { entry ->
                        entry.copy(stored = now).toEntities(uidNumberMapper)
                    }

                val dao = schoolDb.getChangeHistoryDao()

                toUpdate.forEach { item ->
                    dao.insertHistoryWithChanges(
                        history = item.changeHistoryEntity,
                        changes = item.changeEntities
                    )
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<ChangeHistoryEntry> {
        TODO("Not yet implemented")
    }

}