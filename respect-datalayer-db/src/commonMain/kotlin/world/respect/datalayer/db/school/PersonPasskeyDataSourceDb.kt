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
import world.respect.datalayer.school.PersonPasskeyDataSource.GetListParams
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.libutil.util.throwable.withHttpStatus
import kotlin.time.Clock

class PersonPasskeyDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : PersonPasskeyDataSourceLocal {

    private suspend fun upsert(
        list: List<PersonPasskey>,
        forceOverwrite: Boolean
    ) {
        val timeNow = Clock.System.now()

        if(list.any { it.personGuid != authenticatedUser.guid })
            throw IllegalArgumentException("Cannot store passkeys for other user")
                .withHttpStatus(401)

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonPasskeyEntityDao().getLastModifiedByPersonUidAndKeyId(
                        personUidNum = uidNumberMapper(authenticatedUser.guid),
                        passKeyId = it.id
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = timeNow).asEntity(uidNumberMapper)
                }

                schoolDb.getPersonPasskeyEntityDao().takeIf { toUpdate.isNotEmpty() }
                    ?.upsertAsync(toUpdate)
            }
        }
    }

    override suspend fun listAll(
        listParams: GetListParams
    ): DataLoadState<List<PersonPasskey>> {
        return DataReadyState(
            data = schoolDb.getPersonPasskeyEntityDao().findAll(
                personGuidNumber = uidNumberMapper(authenticatedUser.guid),
                includeRevoked = if (listParams.includeRevoked) 1 else 0,
            ).map {
                it.asModel(authenticatedUser.guid)
            }
        )
    }

    override suspend fun updateLocal(
        list: List<PersonPasskey>,
        forceOverwrite: Boolean
    ) {
        upsert(list, false)
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonPasskey> {
        throw IllegalArgumentException("Passkeydatasource: will not find by uid list - not used with RemoteWriteQueue")
    }

    override fun listAllAsFlow(
        listParams: GetListParams
    ): Flow<DataLoadState<List<PersonPasskey>>> {
        return schoolDb.getPersonPasskeyEntityDao().findAllAsFlow(
            personGuidNumber = uidNumberMapper(authenticatedUser.guid),
            includeRevoked = if (listParams.includeRevoked) 1 else 0,
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel(authenticatedUser.guid) }
            )
        }
    }

    override suspend fun store(list: List<PersonPasskey>) {
        upsert(list, true)
    }

}