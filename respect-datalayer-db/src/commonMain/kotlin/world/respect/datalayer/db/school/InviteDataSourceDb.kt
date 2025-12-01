package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.InviteDataSourceLocal
import world.respect.datalayer.school.model.Invite
import kotlin.time.Clock

class InviteDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper
) : InviteDataSourceLocal {

    private suspend fun upsertInvites(
        invites: List<Invite>,
        forceOverwrite: Boolean = false
    ) {
        if (invites.isEmpty()) return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            var numStored = 0
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                invites.map { it.copy(stored = timeStored) }.forEach { invite ->
                    val entities = invite.toEntities(uidNumberMapper)
                    val lastModified = schoolDb.getInviteEntityDao()
                        .getLastModifiedByGuid(entities.inviteEntity.iGuidHash) ?: -1L

                    if (forceOverwrite || entities.inviteEntity.iLastModified > lastModified) {
                        schoolDb.getInviteEntityDao().insert(entities.inviteEntity)
                        numStored++
                    }
                }
            }
        }
    }

    override suspend fun store(list: List<Invite>) {
        upsertInvites(list)
    }

    override suspend fun updateLocal(list: List<Invite>, forceOverwrite: Boolean) {
        upsertInvites(list, forceOverwrite)
    }

    override suspend fun findByUidList(uids: List<String>): List<Invite> {
        val uidNums = uids.map { uidNumberMapper(it) }
        return schoolDb.getInviteEntityDao().findByUidList(uidNums)
            .map { it.toModel() }
    }

    suspend fun findByGuid(guid: String): Invite? {
        return schoolDb.getInviteEntityDao()
            .findByGuidHash(uidNumberMapper(guid))
            ?.toModel()
    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Invite>> {
        return schoolDb.getInviteEntityDao()
            .findByGuidHashAsFlow(uidNumberMapper(guid))
            .map { entity ->
                if (entity != null) {
                    DataReadyState(entity.toModel())
                } else {
                    NoDataLoadedState.notFound()
                }
            }
    }
}

