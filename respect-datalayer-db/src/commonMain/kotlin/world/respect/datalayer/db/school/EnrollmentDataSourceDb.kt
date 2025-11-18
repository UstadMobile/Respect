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
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.shared.DataLayerTags.TAG_DATALAYER
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import world.respect.libutil.util.time.atStartOfDayInMillisUtc
import kotlin.collections.map
import kotlin.time.Clock

class EnrollmentDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : EnrollmentDataSourceLocal {

    private val logPrefix: String by lazy {
        "EnrollmentDataSourceDb(${authenticatedUser.guid})"
    }

    private suspend fun upsertEnrollments(
        enrollments: List<Enrollment>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val timeStored = Clock.System.now()
                val entitiesToStore = enrollments.map {
                    it.copy(stored = timeStored).toEntities(uidNumberMapper)
                }.filter {
                    val lastModInDb = schoolDb.getEnrollmentEntityDao().getLastModifiedByUidNum(
                        it.eUidNum
                    ) ?: 0
                    forceOverwrite || it.eLastModified.toEpochMilliseconds() > lastModInDb
                }

                schoolDb.getEnrollmentEntityDao().upsert(entitiesToStore)
                Napier.d(tag = TAG_DATALAYER) {
                    "$logPrefix: upsert ${entitiesToStore.size}/${enrollments.size} (${enrollments.joinToString { it.uid }}) entities"
                }
            }
        }
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Enrollment> {
        return schoolDb.getEnrollmentEntityDao().findByGuid(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<Enrollment>> {
        return schoolDb.getEnrollmentEntityDao().findByGuidAsFlow(
            uidNumberMapper(guid)
        ).map {
            it?.let {
                DataReadyState(it.toModel())
            } ?: NoDataLoadedState.notFound()
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): DataLoadState<List<Enrollment>> {
        return DataReadyState(
            data = schoolDb.getEnrollmentEntityDao().list(
                since = listParams.common.since?.toEpochMilliseconds() ?: 0,
                uidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                classUidNum = listParams.classUid?.let { uidNumberMapper(it) } ?: 0,
                classUidRoleFlag = listParams.role?.flag ?: 0,
                personUidNum = listParams.personUid?.let { uidNumberMapper(it) } ?: 0,
                includeDeleted = listParams.common.includeDeleted ?: false,
                activeOnDayInUtcMs = listParams.activeOnDay?.atStartOfDayInMillisUtc() ?: 0,
                notRemovedBefore = 0,
            ).map { it.toModel() }
        )
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Enrollment> {
        return IPagingSourceFactory {
            schoolDb.getEnrollmentEntityDao().listAsPagingSource(
                since = listParams.common.since?.toEpochMilliseconds() ?: 0,
                uidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                classUidNum = listParams.classUid?.let { uidNumberMapper(it) } ?: 0,
                classUidRoleFlag = listParams.role?.flag ?: 0,
                personUidNum = listParams.personUid?.let { uidNumberMapper(it) } ?: 0,
                includeDeleted = listParams.common.includeDeleted ?: false,
                activeOnDayInUtcMs = listParams.activeOnDay?.atStartOfDayInMillisUtc() ?: 0,
                notRemovedBefore = 0,
            ).map(
                tag = { "EnrollmentDataSourceDb/list params=$listParams" }
            ) {
                it.toModel()
            }
        }
    }

    override suspend fun store(list: List<Enrollment>) {
        upsertEnrollments(list, false)
    }

    override suspend fun updateLocal(
        list: List<Enrollment>,
        forceOverwrite: Boolean
    ) {
        upsertEnrollments(list, false)
    }

    override suspend fun findByUidList(uids: List<String>): List<Enrollment> {
        return schoolDb.getEnrollmentEntityDao().findByUidNumList(
            uids.map { uidNumberMapper(it) }
        ).map { it.toModel() }
    }


}