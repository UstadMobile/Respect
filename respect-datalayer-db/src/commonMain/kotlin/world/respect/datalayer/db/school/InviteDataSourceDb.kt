package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.InviteDataSourceLocal
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class InviteDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper
) : InviteDataSourceLocal {

    private suspend fun upsertInvites(
        invites: List<Invite2>,
        forceOverwrite: Boolean = false
    ) {
        if (invites.isEmpty()) return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            var numStored = 0
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                invites.forEach { invite ->
                    val entity = invite.toEntity(uidNumberMapper).copy(
                        iStored = timeStored
                    )

                    val lastModified = schoolDb.getInviteEntityDao()
                        .getLastModifiedByGuid(entity.iGuidHash) ?: -1L

                    if (forceOverwrite || entity.iLastModified.toEpochMilliseconds() > lastModified) {
                        schoolDb.getInviteEntityDao().insert(entity)
                        numStored++
                    }
                }
            }
        }
    }
    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: InviteDataSource.GetListParams
    ): IPagingSourceFactory<Int, Invite2> {
        return IPagingSourceFactory {
            schoolDb.getInviteEntityDao().findAllAsPagingSource(
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                code = params.inviteCode,
            ).map {
                it.toModel()
            }
        }
    }
    override suspend fun store(list: List<Invite2>) {
        upsertInvites(list)
    }

    override suspend fun updateLocal(list: List<Invite2>, forceOverwrite: Boolean) {
        upsertInvites(list, forceOverwrite)
    }

    override suspend fun findByUidList(uids: List<String>): List<Invite2> {
        val uidNums = uids.map { uidNumberMapper(it) }
        return schoolDb.getInviteEntityDao().findByUidList(uidNums)
            .map { it.toModel() }
    }

    override suspend fun findByGuid(guid: String): DataLoadState<Invite2> {
        return schoolDb.getInviteEntityDao().findByGuidHash(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override suspend fun findByCode(code: String): DataLoadState<Invite2> {
        return schoolDb.getInviteEntityDao().getInviteByInviteCode(
            code
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Invite2>> {
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

