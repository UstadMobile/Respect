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
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.AssignmentDataSourceLocal
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.time.Clock

class AssignmentDatasourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : AssignmentDataSourceLocal{

    private suspend fun doUpsertAssignment(
        assignment: Assignment,
    ) {
        val entities = assignment.copy(stored = Clock.System.now()).toEntities(uidNumberMapper)

        schoolDb.getAssignmentLearningResourceRefEntityDao().deleteByAssignmentUidNum(
            entities.assignment.aeUidNum
        )
        schoolDb.getAssignmentEntityDao().upsert(listOf(entities.assignment))
        schoolDb.getAssignmentLearningResourceRefEntityDao().upsert(entities.learningUnits)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Assignment>> {
        return schoolDb.getAssignmentEntityDao().findByUidNumAsFlow(
            uidNumberMapper(guid)
        ).map { assignmentEntity ->
            DataLoadState.readyOrNotFoundIfNull(assignmentEntity?.toModel())
        }
    }

    override suspend fun findByGuid(params: DataLoadParams, guid: String): DataLoadState<Assignment> {
        return DataLoadState.readyOrNotFoundIfNull(
            data = schoolDb.getAssignmentEntityDao().findByUidNum(
                uidNumberMapper(guid)
            )?.toModel()
        )
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: AssignmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Assignment> {
        return IPagingSourceFactory {
            schoolDb.getAssignmentEntityDao().listAsPagingSource(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                uidNum = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            ).map { it.toModel() }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: AssignmentDataSource.GetListParams
    ): DataLoadState<List<Assignment>> {
        return DataReadyState(
            schoolDb.getAssignmentEntityDao().list(
                uidNum = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            ).map {
                it.toModel()
            }
        )
    }

    override suspend fun store(list: List<Assignment>) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { assignment ->
                    val lastModAndPermissionInDb = schoolDb.getClassEntityDao()
                        .getLastModifiedAndHasPermission(
                            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                            classUidNum = uidNumberMapper(assignment.classUid),
                            requiredPermission = PermissionFlags.CLASS_WRITE,
                        )

                    if(!lastModAndPermissionInDb.hasPermission)
                        throw ForbiddenException()

                    doUpsertAssignment(assignment)
                }
            }
        }
    }

    override suspend fun updateLocal(
        list: List<Assignment>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { assignment ->
                    val lastModifiedInDb = schoolDb.getAssignmentEntityDao().getLastModifiedByUidNum(
                        uidNumberMapper(assignment.uid)
                    ) ?: -1

                    if(forceOverwrite ||
                            assignment.lastModified.toEpochMilliseconds() > lastModifiedInDb) {
                        doUpsertAssignment(assignment)
                    }
                }
            }
        }

        Napier.d("RPaging/AssignmentDataSourceDb: updatedLocal: ${list.size} assignments")
    }

    override suspend fun findByUidList(uids: List<String>): List<Assignment> {
        return schoolDb.getAssignmentEntityDao().findByUidNums(
            uids.map { uidNumberMapper(it) }
        ).map { it.toModel() }
    }
}