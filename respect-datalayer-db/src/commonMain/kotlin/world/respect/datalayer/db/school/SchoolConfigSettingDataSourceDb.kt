package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSourceLocal
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.shared.maxLastModifiedOrNull
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class SchoolConfigSettingDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : SchoolConfigSettingDataSourceLocal {

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        return schoolDb.getSchoolConfigSettingEntityDao().findByKey(guid)
            ?.asModel()?.let { DataReadyState(it) } ?: NoDataLoadedState.notFound()
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        return schoolDb.getSchoolConfigSettingEntityDao().listAsFlow(
            key = params.key,
            since = params.common.since?.toEpochMilliseconds() ?: 0
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel() }
            )
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolConfigSetting> {
        return IPagingSourceFactory {
            schoolDb.getSchoolConfigSettingEntityDao().listAsPagingSource(
                key = params.key,
                since = params.common.since?.toEpochMilliseconds() ?: 0
            ).map(tag = { "SchoolConfigSettingDataSourceDb/listAsPagingSource(params=$params)" }) {
                it.asModel()
            }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        val queryTime = Clock.System.now()
        val data = schoolDb.getSchoolConfigSettingEntityDao().list(
            key = params.key,
            since = params.common.since?.toEpochMilliseconds() ?: 0
        ).map { it.asModel() }

        return DataReadyState(
            data = data,
            metaInfo = DataLoadMetaInfo(
                lastModified = data.maxLastModifiedOrNull()?.toEpochMilliseconds() ?: -1,
                lastStored = data.maxLastStoredOrNull()?.toEpochMilliseconds() ?: -1,
                consistentThrough = queryTime,
            )
        )
    }

    override suspend fun store(list: List<SchoolConfigSetting>) {
        if (list.isEmpty()) return
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getSchoolConfigSettingEntityDao().insert(
                    list.map { it.copy(stored = Clock.System.now()).asEntity() }
                )
            }
        }
    }

    override suspend fun updateLocal(
        list: List<SchoolConfigSetting>,
        forceOverwrite: Boolean
    ) {
        if (list.isEmpty()) return
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.filter { item ->
                    forceOverwrite || schoolDb.getSchoolConfigSettingEntityDao().getLastModifiedByKey(
                        item.key
                    ).let { it ?: 0L } < item.lastModified.toEpochMilliseconds()
                }.forEach { item ->
                    schoolDb.getSchoolConfigSettingEntityDao().insert(item.asEntity())
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<SchoolConfigSetting> {
        return schoolDb.getSchoolConfigSettingEntityDao().findByKeys(uids).map { it.asModel() }
    }
}
