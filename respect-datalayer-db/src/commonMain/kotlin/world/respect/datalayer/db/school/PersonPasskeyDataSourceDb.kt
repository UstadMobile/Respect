package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.model.PersonPasskey

class PersonPasskeyDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : PersonPasskeyDataSourceLocal {

    override suspend fun listAll(): DataLoadState<List<PersonPasskey>> {
        return DataReadyState(
            data = schoolDb.getPersonPasskeyEntityDao().findAll(
                uidNumberMapper(authenticatedUser.guid)
            ).map {
                it.asModel(authenticatedUser.guid)
            }
        )
    }

    override suspend fun updateLocal(
        list: List<PersonPasskey>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonPasskeyEntityDao().getLastModifiedByPersonUidAndKeyId(
                        personUidNum = uidNumberMapper(authenticatedUser.guid),
                        passKeyId = it.id
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.asEntity(uidNumberMapper)
                }

                schoolDb.getPersonPasskeyEntityDao().takeIf { toUpdate.isNotEmpty() }
                    ?.upsertAsync(toUpdate)
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonPasskey> {
        throw IllegalArgumentException("Passkeydatasource: will not find by uid list")
    }

    override fun listAllAsFlow(): Flow<DataLoadState<List<PersonPasskey>>> {
        return schoolDb.getPersonPasskeyEntityDao().findAllAsFlow(
            uidNumberMapper(authenticatedUser.guid)
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel(authenticatedUser.guid) }
            )
        }
    }

    override suspend fun store(list: List<PersonPasskey>) {
        TODO("Not yet implemented")
    }

}