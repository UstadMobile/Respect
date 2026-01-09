package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.shared.paging.IPagingSourceFactory

class DummySchoolConfigSettingsDataSource: SchoolConfigSettingDataSource {

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        TODO("Not yet implemented")
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolConfigSetting> {
        TODO("Not yet implemented")
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        return flowOf(
            DataReadyState(
                data = listOf(
                    SchoolConfigSetting(
                        key = SchoolConfigSettingDataSource.KEY_APP_CATALOGS,
                        value = "http://10.40.49.70/opds/apps.json"
                    )
                )
            )
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        return DataReadyState(
            data = listOf(
                SchoolConfigSetting(
                    key = SchoolConfigSettingDataSource.KEY_APP_CATALOGS,
                    value = "http://10.40.49.70/opds/apps.json"
                )
            )
        )
    }

    override suspend fun store(list: List<SchoolConfigSetting>) {
        TODO("Not yet implemented")
    }
}