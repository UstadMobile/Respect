package world.respect.datalayer.db.school.domain

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.exceptions.UnauthorizedException
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.model.Person

class CheckPersonPermissionUseCaseDbImpl(
    private val getAuthenticatedPersonUseCase: GetAuthenticatedPersonUseCase,
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
): CheckPersonPermissionUseCase {

    override suspend fun invoke(
        subject: Person,
        permission: Long
    ): Boolean {
        val authenticatedPerson = getAuthenticatedPersonUseCase() ?: throw UnauthorizedException()

        //Admin can do anything
        if(authenticatedPerson.isAdmin())
            return true

        //User can update their own info
        if(authenticatedPerson.guid == subject.guid)
            return true

        return schoolDb.getSchoolPermissionGrantDao().personHasPermission(
            uidNumberMapper(authenticatedPerson.guid), permission
        )
    }
}