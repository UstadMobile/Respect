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
            local.putPersonsLocal(listOf(remote.data))
        }

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        val remoteFlow = remote.findByGuidAsFlow(guid).onEach {
            if(it is DataReadyState) {
                local.putPersonsLocal(listOf(it.data))
            }

            //here: update consistent-through tracker

        }

        return local.findByGuidAsFlow(guid).combineWithRemote(remoteFlow)
    }

    override fun findAllListDetailsAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<PersonListDetails>>> {
        val remoteFlow = remote.findAllAsFlow(loadParams, searchQuery).onEach {
            if(it is DataReadyState) {
                local.putPersonsLocal(it.data)
            }
        }

        val localFlow = local.findAllListDetailsAsFlow(loadParams, searchQuery)
        return localFlow.combineWithRemote(remoteFlow)
    }

    override fun findAllAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Person>>> {
        return local.findAllAsFlow(loadParams, searchQuery)
    }

    override suspend fun findAll(
        loadParams: DataLoadParams,
        searchQuery: String?,
        since: Instant?,
    ): DataLoadState<List<Person>> {
        val remote = remote.findAll(loadParams, searchQuery, since)
        if(remote is DataReadyState) {
            local.putPersonsLocal(remote.data)
            validationHelper.updateValidationInfo(remote.metaInfo)
        }

        return local.findAll(loadParams, searchQuery, since).combineWithRemote(remote)
    }

    override fun findAllAsPagingSource(
        loadParams: DataLoadParams,
        searchQuery: String?,
        since: Instant?,
        guid: String?,
    ): PagingSource<Int, Person> {
        return RepositoryOffsetLimitPagingSource(
            local = local.findAllAsPagingSource(loadParams, searchQuery, since),
            remote = remote.findAllAsPagingSource(loadParams, searchQuery, since),
            argKey = 0,
            mediatorStore = mediatorStore,
            onUpdateLocalFromRemote = local::updateLocalFromRemote,
        )
    }

    override fun findAllListDetailsAsPagingSource(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): PagingSource<Int, PersonListDetails> {
        return RepositoryOffsetLimitPagingSource(
            local = local.findAllListDetailsAsPagingSource(loadParams, searchQuery),
            remote = remote.findAllAsPagingSource(loadParams, searchQuery),
            argKey = 0,
            mediatorStore = mediatorStore,
            onUpdateLocalFromRemote = local::updateLocalFromRemote,
        )
    }

}