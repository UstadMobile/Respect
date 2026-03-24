package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface SchoolConfigSettingDataSource: WritableDataSource<SchoolConfigSetting> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val key: String? = null,
    ) {

        companion object {

            fun fromParams(params: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }

    }

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting>

    fun listAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<SchoolConfigSetting>>>

    fun listAsPagingSource(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): IPagingSourceFactory<Int, SchoolConfigSetting>

    suspend fun list(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): DataLoadState<List<SchoolConfigSetting>>

    override suspend fun store(list: List<SchoolConfigSetting>)

    companion object {

        const val ENDPOINT_NAME = "SchoolConfigSetting"

        const val KEY_APP_CATALOGS = "app-catalogs"

    }
}