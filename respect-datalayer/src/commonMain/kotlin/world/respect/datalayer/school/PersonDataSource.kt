package world.respect.datalayer.school

import androidx.paging.PagingSource
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.params.GetListCommonParams
import kotlin.time.Instant

interface PersonDataSource {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val filterByClazzUid: String? = null,
        val filterByClazzRole: EnrollmentRoleEnum? = null,
    ) {

        companion object {
            fun fromParams(stringValues: StringValues) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(stringValues)
                )
            }
        }

    }

    suspend fun findByUsername(username: String): Person?

    suspend fun findByGuid(loadParams: DataLoadParams, guid: String): DataLoadState<Person>

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>>

    fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
    ): Flow<DataLoadState<List<Person>>>

    suspend fun list(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
        since: Instant? = null,
    ): DataLoadState<List<Person>>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): PagingSource<Int, Person>


    fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): PagingSource<Int, PersonListDetails>

    /**
     * Persists the list to the DataSource. The underlying DataSource WILL set the stored time on
     * the data. It WILL NOT set the last-modified time (this should be done by the ViewModel or
     * UseCase actually changing the data).
     */
    suspend fun store(
        persons: List<Person>
    )


    companion object {

        const val ENDPOINT_NAME = "person"

    }

}