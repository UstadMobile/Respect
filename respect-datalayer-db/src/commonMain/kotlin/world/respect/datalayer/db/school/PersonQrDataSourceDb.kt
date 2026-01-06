package world.respect.datalayer.db.school

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
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.school.PersonQrCodeDataSourceLocal
import world.respect.datalayer.school.PersonQrDataSource
import world.respect.datalayer.school.model.PersonBadge
import kotlin.time.Clock

class PersonQrDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : PersonQrCodeDataSourceLocal {

    private suspend fun upsert(
        list: List<PersonBadge>,
        forceOverwrite: Boolean
    ) {
        val timeNow = Clock.System.now()

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonQrBadgeEntityDao().getLastModifiedByUidNum(
                        uidNum = uidNumberMapper(it.personGuid)
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = timeNow).asEntity(uidNumberMapper)
                }
                schoolDb.getPersonQrBadgeEntityDao().upsertAsyncList(toUpdate)
            }
        }
    }

    override suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: PersonQrDataSource.GetListParams
    ): DataLoadState<List<PersonBadge>> {
        return DataReadyState(
            data = schoolDb.getPersonQrBadgeEntityDao().findAll(
                personGuidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                includeDeleted = listParams.includeDeleted
            ).map {
                it.asModel()
            }
        )
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonBadge>>> {
        return schoolDb.getPersonQrBadgeEntityDao().findAllByPersonGuidAsFlow(
            personGuid = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
            includeDeleted = listParams.includeDeleted
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel() }
            )
        }
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonBadge>> {
        return schoolDb.getPersonQrBadgeEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { personQrEntity ->
            if (personQrEntity != null) {
                DataReadyState(
                    data = personQrEntity.asModel()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }


    override suspend fun existsByQrCodeUrl(url: String, uidNum: Long): Boolean {
        return schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getPersonQrBadgeEntityDao().existsByQrCodeUrlExcludingPerson(url, uidNum)
            }
        }
    }

    override suspend fun store(list: List<PersonBadge>) {
        upsert(list, false)
    }

    override suspend fun updateLocal(
        list: List<PersonBadge>,
        forceOverwrite: Boolean
    ) {
        upsert(list, forceOverwrite)
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonBadge> {
        return schoolDb.getPersonQrBadgeEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map {
            it.asModel()
        }
    }
}