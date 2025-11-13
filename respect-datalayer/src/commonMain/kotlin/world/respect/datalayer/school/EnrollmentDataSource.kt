package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface EnrollmentDataSource: WritableDataSource<Enrollment> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val classUid: String? = null,
        val role: EnrollmentRoleEnum? = null,
        val personUid: String? = null,
    ) {

        companion object {

            fun fromParams(params: StringValues) : GetListParams{
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    classUid = params[DataLayerParams.FILTER_BY_CLASS_UID],
                    role = params[DataLayerParams.FILTER_BY_ENROLLMENT_ROLE]?.let {
                        EnrollmentRoleEnum.fromValue(it)
                    },
                    personUid = params[FILTER_BY_PERSON_UID],
                )
            }

        }

    }

    suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String,
    ): DataLoadState<Enrollment>

    fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String,
    ): Flow<DataLoadState<Enrollment>>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): IPagingSourceFactory<Int, Enrollment>

    override suspend fun store(
        list: List<Enrollment>
    )

    suspend fun deleteEnrollment(uid: String)

    companion object {

        const val ENDPOINT_NAME = "enrollment"

        const val FILTER_BY_PERSON_UID = "filterByPersonUid"

    }

}