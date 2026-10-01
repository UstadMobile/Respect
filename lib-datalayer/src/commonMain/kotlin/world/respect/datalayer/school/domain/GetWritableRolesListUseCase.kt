package world.respect.datalayer.school.domain

import org.openeel.app.userdirectory.model.PersonRoleEnum

interface GetWritableRolesListUseCase {

    suspend operator fun invoke(
        currentPersonRole: PersonRoleEnum
    ): List<PersonRoleEnum>

}