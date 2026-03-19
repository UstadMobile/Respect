package world.respect.datalayer.db.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSourceLocal
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.shared.paging.IPagingSourceFactory

class SchoolConfigSettingDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : SchoolConfigSettingDataSourceLocal {

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        TODO("Not yet implemented")
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        TODO("Not yet implemented")
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolConfigSetting> {
        TODO("Not yet implemented")
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        TODO("Not yet implemented")
    }

    override suspend fun store(list: List<SchoolConfigSetting>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLocal(
        list: List<SchoolConfigSetting>,
        forceOverwrite: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidList(uids: List<String>): List<SchoolConfigSetting> {
        TODO("Not yet implemented")
    }
}