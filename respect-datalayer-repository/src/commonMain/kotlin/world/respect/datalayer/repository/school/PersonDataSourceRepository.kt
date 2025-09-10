package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.PagingSourceMediatorStore
import world.respect.datalayer.repository.shared.paging.RepositoryOffsetLimitPagingSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import kotlin.time.Instant

class PersonDataSourceRepository(
    private val local: PersonDataSourceLocal,
    private val remote: PersonDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
) : PersonDataSource {

    private val mediatorStore = PagingSourceMediatorStore()

    override suspend fun findByUsername(username: String): Person? {
        return local.findByUsername(username)
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        val remote = remote.findByGuid(loadParams, guid)
        if(remote is DataReadyState) {
            local.updateLocalFromRemote(listOf(remote.data))
        }

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        val remoteFlow = remote.findByGuidAsFlow(guid).onEach {
            if(it is DataReadyState) {
                local.updateLocalFromRemote(listOf(it.data))
            }
        }

        return local.findByGuidAsFlow(guid).combineWithRemote(remoteFlow)
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
            local.updateLocalFromRemote(remote.data)
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
            onUpdateLocalFromRemote = local::updateLocalFromRemote,
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
            onUpdateLocalFromRemote = local::updateLocalFromRemote,
        )
    }

    override suspend fun store(persons: List<Person>) {
        local.store(persons)
    }
}