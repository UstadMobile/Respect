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
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.shared.paging.map
import world.respect.libxxhash.XXStringHasher
import kotlin.collections.map
import kotlin.time.Clock

class EnrollmentDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxHash: XXStringHasher,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : EnrollmentDataSourceLocal {

    private suspend fun upsertEnrollments(
        enrollments: List<Enrollment>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val timeStored = Clock.System.now()
                val entities = enrollments.map {
                    it.copy(stored = timeStored).toEntities(xxHash).enrollment
                }.filter {
                    val lastMod = schoolDb.getEnrollmentEntityDao().getLastModifiedByUidNum(
                        it.eUidNum
                    ) ?: 0
                    forceOverwrite || it.eLastModified.toEpochMilliseconds() > lastMod
                }
                schoolDb.getEnrollmentEntityDao().upsert(entities)
            }
        }
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Enrollment> {
        return schoolDb.getEnrollmentEntityDao().findByGuid(
            xxHash.hash(guid)
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<Enrollment>> {
        return schoolDb.getEnrollmentEntityDao().findByGuidAsFlow(
            xxHash.hash(guid)
        ).map {
            it?.let {
                DataReadyState(it.toModel())
            } ?: NoDataLoadedState.notFound()
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): PagingSource<Int, Enrollment> {
        return schoolDb.getEnrollmentEntityDao().listAsPagingSource(
            since = listParams.common.since?.toEpochMilliseconds() ?: 0,
            uidNum = listParams.common.guid?.let { xxHash.hash(it) } ?: 0,
            classUidNum = listParams.classUid?.let { xxHash.hash(it) } ?: 0,
            classUidRoleFlag = listParams.role?.flag ?: 0,
            personUidNum = listParams.personUid?.let { xxHash.hash(it) } ?: 0
        ).map {
            it.toModel()
        }
    }

    override suspend fun store(list: List<Enrollment>) {
        upsertEnrollments(list, false)
    }

    override suspend fun updateLocalFromRemote(
        list: List<Enrollment>,
        forceOverwrite: Boolean
    ) {
        upsertEnrollments(list, false)
    }
}