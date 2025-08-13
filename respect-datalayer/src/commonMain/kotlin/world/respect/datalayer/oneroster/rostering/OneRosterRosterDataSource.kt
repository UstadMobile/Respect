package world.respect.datalayer.oneroster.rostering

import world.respect.datalayer.oneroster.rostering.model.OneRosterClass
import world.respect.datalayer.oneroster.rostering.model.OneRosterEnrollment
import world.respect.datalayer.oneroster.rostering.model.OneRosterUser

interface OneRosterRosterDataSource {

    suspend fun getAllUsers(): List<OneRosterUser>

    suspend fun putUser(user: OneRosterUser)

    suspend fun getAllClasses(): List<OneRosterClass>
    suspend fun getClassBySourcedId(sourcedId: String): OneRosterClass

    suspend fun putClass(clazz: OneRosterClass)

    suspend fun getEnrolmentsByClass(classSourcedId: String)

    suspend fun putEnrollment(enrollment: OneRosterEnrollment)

}