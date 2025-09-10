package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.oneroster.OneRosterDataSource
import world.respect.datalayer.school.PersonDataSource

class SchoolDataSourceRepository(
    private val local: SchoolDataSourceLocal,
    private val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
) : SchoolDataSource {

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceRepository(
            local.personDataSource, remote.personDataSource, validationHelper
        )
    }

    override val onRoasterDataSource: OneRosterDataSource
        get() = TODO("Not yet implemented")
}