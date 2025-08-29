package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.oneroster.OneRosterDataSource
import world.respect.datalayer.school.PersonDataSource

class SchoolDataSourceRepository(
    private val local: SchoolDataSourceLocal,
    private val remote: SchoolDataSource,
) : SchoolDataSource {

    override val personDataSource: PersonDataSource
        get() = TODO("Not yet implemented")

    override val onRoasterDataSource: OneRosterDataSource
        get() = TODO("Not yet implemented")
}