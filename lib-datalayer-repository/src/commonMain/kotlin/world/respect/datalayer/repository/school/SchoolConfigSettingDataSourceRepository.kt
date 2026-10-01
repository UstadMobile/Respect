package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSourceLocal
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class SchoolConfigSettingDataSourceRepository(
    override val local: SchoolConfigSettingDataSourceLocal,
    override val remote: SchoolConfigSettingDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : SchoolConfigSettingDataSource, RepositoryModelDataSource<SchoolConfigSetting> {

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        if (!params.onlyIfCached) {
            val remoteResult = remote.findByGuid(params, guid)
            local.updateFromRemoteIfNeeded(remoteResult, validationHelper)
        }
        return local.findByGuid(params, guid)
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        return local.listAsFlow(loadParams, params)
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        val remoteResult = remote.list(loadParams, params)
        if (remoteResult is DataReadyState) {
            local.updateLocal(remoteResult.data)
            validationHelper.updateValidationInfo(remoteResult.metaInfo)
        }

        return local.list(loadParams, params).combineWithRemote(remoteResult)
    }

    override suspend fun store(list: List<SchoolConfigSetting>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.SCHOOL_CONFIG_SETTING,
                    uid = it.key,
                    timeQueued = timeNow,
                )
            }
        )
    }
}
