package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.datalayer.school.SchoolAppDataSourceLocal
import world.respect.datalayer.school.model.SchoolApp
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map

class SchoolAppDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : SchoolAppDataSourceLocal {

    suspend fun upsertSchoolApps(
        schoolApps: List<SchoolApp>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val toUpsert = schoolApps.map {
                    it.toEntity(uidNumberMapper)
                }.filter {
                    val lastModInDb = schoolDb.getSchoolAppEntityDao().getLastModifiedByUidNum(
                        it.saUidNum
                    )
                    forceOverwrite || (lastModInDb ?: 0) < it.saLastModified.toEpochMilliseconds()
                }

                schoolDb.getSchoolAppEntityDao().takeIf { toUpsert.isNotEmpty() }?.upsert(toUpsert)
            }
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolApp> {
        return IPagingSourceFactory {
            schoolDb.getSchoolAppEntityDao().listAsPagingSource(
                includeDeleted = params.includeDeleted
            ).map {
                it.toModel()
            }
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolApp>>> {
        return schoolDb.getSchoolAppEntityDao().listAsFlow(
            includeDeleted = params.includeDeleted
        ).map { entityList ->
            DataReadyState(
                data = entityList.map { it.toModel() }
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): DataLoadState<List<SchoolApp>> {
        return DataReadyState(
            data = schoolDb.getSchoolAppEntityDao().list(
                includeDeleted = params.includeDeleted
            ).map { it.toModel() },
        )
    }

    override suspend fun store(list: List<SchoolApp>) {
        upsertSchoolApps(list, false)
    }

    override suspend fun updateLocal(
        list: List<SchoolApp>,
        forceOverwrite: Boolean
    ) {
        upsertSchoolApps(list, forceOverwrite)
    }

    override suspend fun findByUidList(uids: List<String>): List<SchoolApp> {
        return schoolDb.getSchoolAppEntityDao().findByUidNumList(
            uids.map { uidNumberMapper(it) }
        ).map { it.toModel() }
    }

}