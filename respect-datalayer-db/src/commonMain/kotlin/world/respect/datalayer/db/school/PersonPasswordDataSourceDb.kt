package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.PersonPasswordDataSourceLocal
import world.respect.datalayer.school.model.PersonPassword
import kotlin.time.Clock

class PersonPasswordDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : PersonPasswordDataSourceLocal{

    private suspend fun upsert(
        list: List<PersonPassword>,
        forceOverwrite: Boolean
    ) {
        val timeNow = Clock.System.now()

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonPasswordEntityDao().getLastModifiedByPersonUidNum(
                        uidNum = uidNumberMapper(it.personGuid)
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = timeNow).asEntity(uidNumberMapper)
                }

                schoolDb.getPersonPasswordEntityDao().upsertAsyncList(toUpdate)
            }
        }

    }

    override suspend fun store(list: List<PersonPassword>) {
        upsert(list, false)
    }

    override suspend fun updateLocal(
        list: List<PersonPassword>,
        forceOverwrite: Boolean
    ) {
        upsert(list, false)
    }

    override suspend fun listAll(listParams: PersonPasswordDataSource.GetListParams): DataLoadState<List<PersonPassword>> {
        return DataReadyState(
            data = schoolDb.getPersonPasswordEntityDao().findAll(
                personGuidNum = uidNumberMapper(authenticatedUser.guid)
            ).map {
                it.asModel()
            }
        )
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonPassword> {
        return schoolDb.getPersonPasswordEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map {
            it.asModel()
        }
    }
}