package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

interface ReportDataSource : WritableDataSource<Report>  {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    )

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


    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>>

    suspend fun delete(guid: String)

    companion object {
        const val ENDPOINT_NAME = "report"
    }
}
