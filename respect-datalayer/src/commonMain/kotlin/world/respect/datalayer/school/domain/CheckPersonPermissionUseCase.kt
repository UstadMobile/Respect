package world.respect.datalayer.school.domain

import world.respect.datalayer.school.model.Person

/**
 * The CheckPersonUseCase is bound (scoped) to a specified authenticated person (the same as the
 * SchoolDataSource itself).
 */
interface CheckPersonPermissionUseCase {

    suspend operator fun invoke(
        subject: Person,
        permission: Long,
    ): Boolean

}