package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface ReportDataSource : WritableDataSource<Report> {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {
        companion object {

            fun fromParams(params: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                )
            }
        }
    }

    /**
     * @param template if true, get list of templates. Otherwise list of reports for the active user
     */

    fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: GetListParams,
        template: Boolean = false
    ): Flow<DataLoadState<List<Report>>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Report>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
        template: Boolean = false
    ): IPagingSourceFactory<Int, Report>


    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>>

    suspend fun delete(guid: String)

    override suspend fun store(
        list: List<Report>
    )

    suspend fun initializeTemplates(idGenerator: () -> String)


    companion object {
        const val ENDPOINT_NAME = "report"
    }
}
