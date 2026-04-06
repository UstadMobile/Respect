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
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSourceLocal
import world.respect.datalayer.school.ext.foldToFlag
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.shared.maxLastModifiedOrNull
import world.respect.datalayer.shared.maxLastStoredOrNull
import kotlin.time.Clock

class SchoolConfigSettingDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val uidNumberMapper: UidNumberMapper,
) : SchoolConfigSettingDataSourceLocal {

    private val authenticatedUserUidNum: Long
        get() = uidNumberMapper(authenticatedUser.guid)

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        return schoolDb.getSchoolConfigSettingEntityDao().list(
            authenticatedPersonUidNum = authenticatedUserUidNum,
            keys = listOf(guid)
        ).firstOrNull()?.asModel()?.let { DataReadyState(it) } ?: NoDataLoadedState.notFound()
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        return schoolDb.getSchoolConfigSettingEntityDao().listAsFlow(
            authenticatedPersonUidNum = authenticatedUserUidNum,
            keys = params.keys,
            since = params.common.since?.toEpochMilliseconds() ?: 0
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel() }
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        val queryTime = Clock.System.now()
        val data = schoolDb.getSchoolConfigSettingEntityDao().list(
            authenticatedPersonUidNum = authenticatedUserUidNum,
            keys = params.keys,
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
                list.forEach { setting ->
                    val lastModAndPermission = schoolDb.getSchoolConfigSettingEntityDao()
                        .getLastModifiedAndHasPermission(
                            authenticatedPersonUidNum = authenticatedUserUidNum,
                            key = setting.key,
                            canWriteRolesMask = setting.canWrite.foldToFlag()
                        )

                    if (!lastModAndPermission.hasPermission) {
                        throw ForbiddenException()
                    }
                }

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
                val toInsert = list.filter { item ->
                    forceOverwrite || schoolDb.getSchoolConfigSettingEntityDao()
                        .getLastModifiedByKey(
                            key = item.key
                        ).let { it ?: 0L } < item.lastModified.toEpochMilliseconds()
                }.map { it.asEntity() }

                if (toInsert.isNotEmpty()) {
                    schoolDb.getSchoolConfigSettingEntityDao().insert(toInsert)
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<SchoolConfigSetting> {
        return schoolDb.getSchoolConfigSettingEntityDao().list(
            authenticatedPersonUidNum = authenticatedUserUidNum,
            keys = uids
        ).map { it.asModel() }
    }
}
