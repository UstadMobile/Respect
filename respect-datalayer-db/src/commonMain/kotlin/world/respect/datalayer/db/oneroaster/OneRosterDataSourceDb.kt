package world.respect.datalayer.db.oneroaster

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.oneroaster.adapter.OneRoasterClassEntities
import world.respect.datalayer.db.oneroaster.adapter.OneRoasterUserEntities
import world.respect.datalayer.db.oneroaster.adapter.toEntities
import world.respect.datalayer.db.oneroaster.adapter.toModel
import world.respect.datalayer.oneroster.OneRosterDataSourceLocal
import world.respect.datalayer.oneroster.model.OneRosterClass
import world.respect.datalayer.oneroster.model.OneRosterEnrollment
import world.respect.datalayer.oneroster.model.OneRosterUser
import world.respect.datalayer.oneroster.composites.ClazzListDetails
import world.respect.libxxhash.XXStringHasher

class OneRosterDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxHash: XXStringHasher,
) : OneRosterDataSourceLocal {

    override suspend fun getAllUsers(): List<OneRosterUser> {
        return schoolDb.getOneRoasterEntityDao().getAllUsers().map {
            OneRoasterUserEntities(it).toModel()
        }
    }

    override suspend fun putUser(user: OneRosterUser) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllClasses(): List<OneRosterClass> {
        return schoolDb.getOneRoasterEntityDao()
            .getAllClasses()
            .map { OneRoasterClassEntities(it).toModel() }
    }

    override suspend fun getClassBySourcedId(sourcedId: String): OneRosterClass {
        TODO("Not yet implemented")
    }

    override suspend fun findClassBySourcedId(
        loadParams: DataLoadParams,
        sourcedId: String
    ): DataLoadState<OneRosterClass> {
        return schoolDb.getOneRoasterEntityDao().findClassBySourcedId(sourcedId).let {
            OneRoasterClassEntities(it)
        }.toModel().let { DataReadyState(it) }
    }

    override suspend fun findClassBySourcedIdAsFlow(sourcedId: String): Flow<DataLoadState<OneRosterClass>> {
        return schoolDb.getOneRoasterEntityDao().findClassBySourcedIdAsFlow(sourcedId = sourcedId)
            .map { oneRosterClassEntity ->
                if (oneRosterClassEntity != null) {
                    DataReadyState(OneRoasterClassEntities(oneRosterClassEntity).toModel())
                } else {
                    NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
                }
            }
    }

    override suspend fun putClass(clazz: OneRosterClass) {
        val entities = clazz.toEntities(xxHash)

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getOneRoasterEntityDao().insert(entities.oneRoasterClassEntity)
            }
        }
    }

    override suspend fun getEnrolmentsByClass(classSourcedId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun putEnrollment(enrollment: OneRosterEnrollment) {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<ClazzListDetails>>> {
        return schoolDb.getOneRoasterEntityDao().findAllListDetailsAsFlow().map {
            DataReadyState(it)
        }
    }

}