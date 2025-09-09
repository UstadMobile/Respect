package world.respect.datalayer.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import kotlin.time.Instant

interface PersonDataSource {

    suspend fun findByUsername(username: String): Person?

    suspend fun findByGuid(loadParams: DataLoadParams, guid: String): DataLoadState<Person>

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>>

    /**
     * Get a list of all the persons in the realm that can be accessed by the DataSource's
     * account.
     *
     * @param loadParams
     * @param searchQuery search text (if any)
     */
    fun findAllListDetailsAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
    ): Flow<DataLoadState<List<PersonListDetails>>>

    fun findAllAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
    ): Flow<DataLoadState<List<Person>>>

    suspend fun findAll(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
        since: Instant? = null,
    ): DataLoadState<List<Person>>

    fun findAllAsPagingSource(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
        since: Instant? = null,
        guid: String? = null,
    ): PagingSource<Int, Person>


}