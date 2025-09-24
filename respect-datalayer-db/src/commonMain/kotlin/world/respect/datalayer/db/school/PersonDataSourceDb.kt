package world.respect.datalayer.db.school

import androidx.paging.PagingSource
import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.PersonEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.maxLastModifiedOrNull
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.datalayer.shared.paging.map
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.time.Clock
import kotlin.time.Instant

class PersonDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
): PersonDataSourceLocal {

    private suspend fun upsertPersons(
        persons: List<Person>,
        forceOverwrite: Boolean = false,
    ) {
        if(persons.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                persons.map { it.copy(stored = timeStored) }.forEach { person ->
                    val entities = person.toEntities(uidNumberMapper)
                    val lastModified = schoolDb.getPersonEntityDao().getLastModifiedByGuid(
                        entities.personEntity.pGuidHash
                    ) ?: -1

                    if(forceOverwrite || entities.personEntity.pLastModified > lastModified) {
                        schoolDb.getPersonEntityDao().insert(entities.personEntity)
                        schoolDb.getPersonRoleEntityDao().deleteByPersonGuidHash(
                            entities.personEntity.pGuidHash
                        )
                        schoolDb.getPersonRoleEntityDao().upsertList(
                            entities.personRoleEntities
                        )
                    }
                }
            }
        }
    }


    override suspend fun store(list: List<Person>) {
        upsertPersons(list)
    }

    override suspend fun findByUsername(username: String): Person? {
        return schoolDb.getPersonEntityDao().findByUsername(username)?.toPersonEntities()?.toModel()
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        return schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(guid))
            ?.toPersonEntities()?.toModel()?.let { DataReadyState(it) } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        return schoolDb.getPersonEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { personEntity ->
            if(personEntity != null) {
                DataReadyState(
                    data = personEntity.toPersonEntities().toModel()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun updateLocalFromRemote(
        list: List<Person>,
        forceOverwrite: Boolean
    ) {
        upsertPersons(list, forceOverwrite)
    }

    override suspend fun findByUidList(uids: List<String>): List<Person> {
        return schoolDb.getPersonEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map { it.toPersonEntities().toModel() }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Person>>> {
        return schoolDb.getPersonEntityDao().findAllAsFlow().map { list ->
            DataReadyState(
                data = list.map {
                    it.toPersonEntities().toModel()
                }
            )
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): PagingSource<Int, Person> {
        return schoolDb.getPersonEntityDao().findAllAsPagingSource(
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
            inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
        ).map(tag = "persondb-mapped") {
            it.toPersonEntities().toModel()
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        searchQuery: String?,
        since: Instant?,
    ): DataLoadState<List<Person>> {
        val queryTime = systemTimeInMillis()
        val data = schoolDb.getPersonEntityDao().findAll(
            since = since?.toEpochMilliseconds() ?: 0,
        ).map {
            PersonEntities(it).toModel()
        }

        return DataReadyState(
            data = data,
            metaInfo = DataLoadMetaInfo(
                lastModified = data.maxLastModifiedOrNull()?.toEpochMilliseconds() ?: -1,
                lastStored = data.maxLastStoredOrNull()?.toEpochMilliseconds() ?: -1,
                consistentThrough = queryTime,
            )
        )
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams,
    ): PagingSource<Int, PersonListDetails> {
        return schoolDb.getPersonEntityDao().findAllListDetailsAsPagingSource()
    }
}