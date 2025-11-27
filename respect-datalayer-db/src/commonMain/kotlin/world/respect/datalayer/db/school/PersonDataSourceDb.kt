package world.respect.datalayer.db.school

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
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.exceptions.UnauthorizedException
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.ext.writePermissionFlag
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.DataLayerTags.TAG_DATALAYER
import world.respect.datalayer.shared.maxLastModifiedOrNull
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PermissionCheckPagingSource
import world.respect.datalayer.shared.paging.map
import world.respect.libutil.util.time.atStartOfDayInMillisUtc
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.time.Clock

class PersonDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
): PersonDataSourceLocal {


    private suspend fun doUpsertPerson(
        person: Person
    ) {
        val entities = person.copy(stored = Clock.System.now()).toEntities(uidNumberMapper)

        schoolDb.getPersonEntityDao().insert(entities.personEntity)

        schoolDb.getPersonRoleEntityDao().deleteByPersonGuidHash(
            entities.personEntity.pGuidHash
        )
        schoolDb.getPersonRoleEntityDao().upsertList(
            entities.personRoleEntities
        )

        schoolDb.getPersonRelatedPersonEntityDao().deleteByPersonUidNum(
            entities.personEntity.pGuidHash
        )
        schoolDb.getPersonRelatedPersonEntityDao().upsert(
            entities.relatedPersonEntities
        )
    }

    override suspend fun store(list: List<Person>) {
        if(list.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { personToStore ->
                    val personInDb = schoolDb.getPersonEntityDao().findByGuidNum(
                        uidNumberMapper(personToStore.guid)
                    )?.toPersonEntities()?.toModel()

                    //Check that if person is in db, role is not being changed.
                    if(personInDb != null &&
                        (personInDb.primaryRole() != personToStore.primaryRole() ||
                                personToStore.roles.size != 1)
                    ) {
                        throw UnauthorizedException("Role cannot be changed, and must have one role")
                    }

                    if(
                        !checkPersonPermissionUseCase(
                            subject = personToStore,
                            permission = personToStore.primaryRole().writePermissionFlag
                        )
                     ) {
                        throw ForbiddenException()
                    }

                    //Check that roles have not been change
                    doUpsertPerson(personToStore)
                }
            }
        }
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

    override suspend fun updateLocal(
        list: List<Person>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE){
                list.filter { person ->
                    forceOverwrite || schoolDb.getPersonEntityDao().getLastModifiedByGuid(
                        uidNumberMapper(person.guid)
                    ).let { it ?: 0L } < person.lastModified.toEpochMilliseconds()
                }.forEach { person ->
                    doUpsertPerson(person)
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<Person> {
        return schoolDb.getPersonEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map { it.toPersonEntities().toModel() }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> {
        return schoolDb.getPersonEntityDao().listAsFlow(
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
            inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
            inClassOnDayInUtcMs = params.inClassOnDay?.atStartOfDayInMillisUtc() ?: 0,
            filterByName = params.filterByName,
            filterByPersonRole = params.filterByPersonRole?.flag ?: 0,
            includeRelated = params.includeRelated,
            includeDeleted = params.common.includeDeleted ?: false,
        ).map { list ->
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
    ): IPagingSourceFactory<Int, Person> {
        return IPagingSourceFactory {
            PermissionCheckPagingSource(
                src = schoolDb.getPersonEntityDao().listAsPagingSource(
                    since = params.common.since?.toEpochMilliseconds() ?: 0,
                    guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                    inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
                    inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
                    inClassOnDayInUtcMs = params.inClassOnDay?.atStartOfDayInMillisUtc() ?: 0,
                    filterByName = params.filterByName,
                    filterByPersonRole = params.filterByPersonRole?.flag ?: 0,
                    includeRelated = params.includeRelated,
                    includeDeleted = params.common.includeDeleted ?: false,
                ).map(tag = { "PersonDataSourceDb/listAsPagingSource(params=$params)" }) {
                    it.toPersonEntities().toModel()
                },
                onCheckPermission = {
                    params.common.guid?.let { guid ->
                        schoolDb.getPersonEntityDao().userCanReadOther(
                            authenticatedUidNum = uidNumberMapper(authenticatedUser.guid),
                            uidNum = uidNumberMapper(guid)
                        )
                    } ?: true
                }
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): DataLoadState<List<Person>> {
        val queryTime = systemTimeInMillis()
        val data = schoolDb.getPersonEntityDao().list(
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
            inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
            inClassOnDayInUtcMs = params.inClassOnDay?.atStartOfDayInMillisUtc() ?: 0,
            filterByName = params.filterByName,
            filterByPersonRole = params.filterByPersonRole?.flag ?: 0,
            includeRelated = params.includeRelated,
            includeDeleted = params.common.includeDeleted ?: false,
        ).map {
            it.toPersonEntities().toModel()
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
    ): IPagingSourceFactory<Int, PersonListDetails> {
        return IPagingSourceFactory {
            schoolDb.getPersonEntityDao().findAllListDetailsAsPagingSource(
                since = listParams.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                inClazzGuidHash = listParams.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
                inClazzRoleFlag = listParams.filterByEnrolmentRole?.flag ?: 0,
                filterByName = listParams.filterByName,
                filterByPersonRole = listParams.filterByPersonRole?.flag ?: 0,
                includeRelated = listParams.includeRelated,
            )
        }
    }

}