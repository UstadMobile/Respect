package world.respect.datalayer.db.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.ChangeHistoryDataSource
import world.respect.datalayer.school.ChangeHistoryLocal
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map

class ChangeHistoryDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    ) : ChangeHistoryLocal {



    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<ChangeHistoryEntry> {
        return schoolDb.getChangeHistoryDao()
            .findByGuid(guid)?.let {
            DataReadyState(it.toModel())
        }?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<ChangeHistoryEntry>> {
        return schoolDb.getChangeHistoryDao()
            .findByGuidAsFlow(guid).map {
                it?.let {
                    DataReadyState(it.toModel())
                } ?: NoDataLoadedState.notFound()
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

    override suspend fun store(list: List<ChangeHistoryEntry>) {
    }

    override suspend fun updateLocal(
        list: List<ChangeHistoryEntry>,
        forceOverwrite: Boolean
    ) {
    }

    override suspend fun findByUidList(uids: List<String>): List<ChangeHistoryEntry> {
        TODO("Not yet implemented")
    }

}