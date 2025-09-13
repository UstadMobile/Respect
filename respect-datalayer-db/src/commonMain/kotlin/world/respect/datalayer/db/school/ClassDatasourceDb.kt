package world.respect.datalayer.db.school

import androidx.paging.PagingSource
import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.ClassEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.model.Clazz
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
        @Suppress("unused") forceOverwrite: Boolean
    ) {
        if(classes.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                classes.map { it.copy(stored = timeStored) }.forEach { clazz ->
                    val entities = clazz.toEntities(uidNumberMapper)
                    schoolDb.getClassEntityDao().upsert(entities.clazz)
                }
            }

        }
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>> {
        return schoolDb.getClassEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { classEntity ->
            classEntity?.let { ClassEntities(it) }?.toModel()?.let {
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
            DataReadyState(ClassEntities(it).toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): PagingSource<Int, Clazz> {
        return schoolDb.getClassEntityDao().findAllAsPagingSource(
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
        ).map {
            ClassEntities(it).toModel()
        }
    }

    override suspend fun store(classes: List<Clazz>) {
        upsertClasses(classes, false)
    }

    override suspend fun updateLocalFromRemote(
        list: List<Clazz>,
        forceOverwrite: Boolean
    ) {
        upsertClasses(list, false)
    }

    override suspend fun findByUidList(uids: List<String>): List<Clazz> {
        TODO("Not yet implemented")
    }
}