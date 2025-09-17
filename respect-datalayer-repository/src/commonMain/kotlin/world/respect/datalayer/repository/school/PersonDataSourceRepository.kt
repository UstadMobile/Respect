package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.PagingSourceMediatorStore
import world.respect.datalayer.repository.shared.paging.RepositoryOffsetLimitPagingSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import kotlin.time.Instant
import world.respect.libutil.util.time.systemTimeInMillis

class PersonDataSourceRepository(
    override val local: PersonDataSourceLocal,
    override val remote: PersonDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : PersonDataSource, RepositoryModelDataSource<Person> {

    private val mediatorStore = PagingSourceMediatorStore()

    override suspend fun findByUsername(username: String): Person? {
        return local.findByUsername(username)
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        val remote = remote.findByGuid(loadParams, guid)
        local.updateFromRemoteIfNeeded(
            remote, validationHelper
        )

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Person>>> {
        return local.listAsFlow(loadParams, searchQuery)
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        searchQuery: String?,
        since: Instant?,
    ): DataLoadState<List<Person>> {
        val remote = remote.list(loadParams, searchQuery, since)
        if(remote is DataReadyState) {
            local.updateLocal(remote.data)
            validationHelper.updateValidationInfo(remote.metaInfo)
        }

        return local.list(loadParams, searchQuery, since).combineWithRemote(remote)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): PagingSource<Int, Person> {
        return RepositoryOffsetLimitPagingSource(
            local = local.listAsPagingSource(loadParams, params),
            remote = remote.listAsPagingSource(loadParams, params),
            argKey = 0,
            mediatorStore = mediatorStore,
            onUpdateLocalFromRemote = local::updateLocal,
        )
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams
    ): PagingSource<Int, PersonListDetails> {
        return RepositoryOffsetLimitPagingSource(
            local = local.listDetailsAsPagingSource(loadParams, listParams),
            remote = remote.listAsPagingSource(loadParams, listParams),
            argKey = 0,
            mediatorStore = mediatorStore,
            onUpdateLocalFromRemote = local::updateLocal,
        )
    }

    override suspend fun store(list: List<Person>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON,
                    uid = it.guid,
                    timestamp = timeNow,
                )
            }
        )
    }
}