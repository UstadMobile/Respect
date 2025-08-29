package world.respect.datalayer.repository.oneroaster

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.oneroster.OneRosterDataSourceLocal
import world.respect.datalayer.oneroster.OneRosterDataSource
import world.respect.datalayer.oneroster.model.OneRosterClass
import world.respect.datalayer.oneroster.model.OneRosterEnrollment
import world.respect.datalayer.oneroster.model.OneRosterUser
import world.respect.datalayer.oneroster.composites.ClazzListDetails

class OneRoasterDataSourceRepository(
    private val local: OneRosterDataSourceLocal,
    private val remote: OneRosterDataSource,
) :OneRosterDataSource{

    override suspend fun getAllUsers(): List<OneRosterUser> {
        TODO("Not yet implemented")
    }

    override suspend fun putUser(user: OneRosterUser) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllClasses(): List<OneRosterClass> {
        TODO("Not yet implemented")
    }

    override suspend fun getClassBySourcedId(sourcedId: String): OneRosterClass {
        TODO("Not yet implemented")
    }

    override suspend fun findClassBySourcedId(
        loadParams: DataLoadParams,
        sourcedId: String
    ): DataLoadState<OneRosterClass> {
        TODO("Not yet implemented")
    }

    override suspend fun findClassBySourcedIdAsFlow(guid: String): Flow<DataLoadState<OneRosterClass>> {
        TODO("Not yet implemented")
    }

    override suspend fun putClass(clazz: OneRosterClass) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}