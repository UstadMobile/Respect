package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams
import kotlin.time.Instant

interface PersonDataSource: WritableDataSource<Person> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val filterByClazzUid: String? = null,
        val filterByEnrolmentRole: EnrollmentRoleEnum? = null,
        val filterByName: String? = null,
        val filterByPersonRole: PersonRoleEnum? = null,
    ) {

        companion object {
            fun fromParams(stringValues: StringValues) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(stringValues),
                    filterByClazzUid = stringValues[DataLayerParams.FILTER_BY_CLASS_UID],
                    filterByName = stringValues[DataLayerParams.SEARCH_QUERY],
                    filterByEnrolmentRole = stringValues[DataLayerParams.FILTER_BY_ENROLLMENT_ROLE]?.let {
                        EnrollmentRoleEnum.fromValue(it)
                    },
                    filterByPersonRole = stringValues[FILTER_BY_PERSON_ROLE]?.let {
                        PersonRoleEnum.fromValue(it)
                    },
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
    ): IPagingSourceFactory<Int, Person>


    fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): IPagingSourceFactory<Int, PersonListDetails>


    fun listPersonRelatedFamilyMembersAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<List<Person>>>

    /**
     * Persists the list to the DataSource. The underlying DataSource WILL set the stored time on
     * the data. It WILL NOT set the last-modified time (this should be done by the ViewModel or
     * UseCase actually changing the data).
     */
    override suspend fun store(
        list: List<Person>
    )


    companion object {

        const val ENDPOINT_NAME = "person"


        const val FILTER_BY_PERSON_ROLE = "filterByPersonRole"


    }

}