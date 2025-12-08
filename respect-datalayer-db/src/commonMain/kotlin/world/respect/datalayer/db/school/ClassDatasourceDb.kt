package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toClassEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class ClassDatasourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : ClassDataSourceLocal {

    private suspend fun upsertClasses(
        classes: List<Clazz>,
        forceOverwrite: Boolean = false,
    ) {
        if(classes.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()

            var numUpdated = 0
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                classes.map { it.copy(stored = timeStored) }.forEach { clazz ->
                    val entities = clazz.toEntities(uidNumberMapper)
                    val lastModifiedInDb = schoolDb.getClassEntityDao().getLastModifiedByGuid(
                        entities.clazz.cGuidHash
                    ) ?: -1

                    if(forceOverwrite ||
                            entities.clazz.cLastModified.toEpochMilliseconds() > lastModifiedInDb) {
                        schoolDb.getClassPermissionEntityDao().deleteByClassUidNum(
                            classUidNum = entities.clazz.cGuidHash
                        )
                        schoolDb.getClassEntityDao().upsert(entities.clazz)
                        schoolDb.getClassPermissionEntityDao().upsertList(
                            permissionsList = entities.permissionEntities
                        )

                        numUpdated++
                    }
                }
            }
            Napier.d("RPaging/ClassDataSourceDb: updated: $numUpdated/${classes.size}")

        }
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>> {
        return schoolDb.getClassEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { classEntity ->
            classEntity?.toClassEntities()?.toModel()?.let {
                DataReadyState(it)
            } ?: NoDataLoadedState.notFound()
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz> {
        return schoolDb.getClassEntityDao().findByGuid(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(it.toClassEntities().toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): IPagingSourceFactory<Int, Clazz> {
        return IPagingSourceFactory {
            schoolDb.getClassEntityDao().listAsPagingSource(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = params.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                code = params.inviteCode,
            ).map {
                it.toClassEntities().toModel()
            }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): DataLoadState<List<Clazz>> {
        return DataReadyState(
            data = schoolDb.getClassEntityDao().list(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = params.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                code = params.inviteCode,
            ).map {
                it.toClassEntities().toModel()
            }
        )
    }

    override suspend fun store(list: List<Clazz>) {
        upsertClasses(list, false)
    }

    override suspend fun updateLocal(
        list: List<Clazz>,
        forceOverwrite: Boolean
    ) {
        upsertClasses(list, false)
    }

    override suspend fun findByUidList(uids: List<String>): List<Clazz> {
        return schoolDb.getClassEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map {
            it.toClassEntities().toModel()
        }
    }
}