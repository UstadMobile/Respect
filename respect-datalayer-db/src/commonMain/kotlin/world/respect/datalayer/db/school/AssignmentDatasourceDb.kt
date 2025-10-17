package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.AssignmentDataSourceLocal
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class AssignmentDatasourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : AssignmentDataSourceLocal{


    private suspend fun upsertAssignments(
        assignments: List<Assignment>,
        forceOverwrite: Boolean,
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val timeStored = Clock.System.now()
                val entities = assignments.map {
                    it.copy(stored = timeStored).toEntities(uidNumberMapper)
                }.filter {
                    val lastModInDb: Long = schoolDb.getAssignmentEntityDao().getLastModifiedByUidNum(
                        it.assignment.aeUidNum
                    ) ?: 0
                    forceOverwrite || it.assignment.aeLastModified.toEpochMilliseconds() > lastModInDb
                }

                schoolDb.getAssignmentEntityDao().upsert(entities.map { it.assignment })
                entities.forEach {
                    schoolDb.getAssignmentAssigneeRefEntityDao().deleteByAssignmentUidNum(it.assignment.aeUidNum)
                    schoolDb.getAssignmentAssigneeRefEntityDao().upsert(it.assignees)
                    schoolDb.getAssignmentLearningResourceRefEntityDao().deleteByAssignmentUidNum(
                        it.assignment.aeUidNum
                    )
                    schoolDb.getAssignmentLearningResourceRefEntityDao().upsert(it.learningUnits)
                }
            }
        }
    }

    override suspend fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: AssignmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Assignment> {
        return IPagingSourceFactory {
            schoolDb.getAssignmentEntityDao().listAsPagingSource().map { it.toModel() }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: AssignmentDataSource.GetListParams
    ): DataLoadState<List<Assignment>> {
        return DataReadyState(
            schoolDb.getAssignmentEntityDao().list().map {
                it.toModel()
            }
        )
    }

    override suspend fun store(list: List<Assignment>) {
        upsertAssignments(list, false)
    }

    override suspend fun updateLocal(
        list: List<Assignment>,
        forceOverwrite: Boolean
    ) {
        upsertAssignments(list, forceOverwrite)
    }

    override suspend fun findByUidList(uids: List<String>): List<Assignment> {
        return schoolDb.getAssignmentEntityDao().findByUidNums(
            uids.map { uidNumberMapper(it) }
        ).map { it.toModel() }
    }
}