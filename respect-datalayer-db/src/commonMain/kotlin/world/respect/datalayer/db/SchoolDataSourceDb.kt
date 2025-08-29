package world.respect.datalayer.db

import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.oneroaster.OneRosterDataSourceDb
import world.respect.datalayer.db.school.PersonDataSourceDb
import world.respect.datalayer.oneroster.OneRosterDataSourceLocal
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.libxxhash.XXStringHasher

class SchoolDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher,
) : SchoolDataSourceLocal{

    override val personDataSource: PersonDataSourceLocal by lazy {
        PersonDataSourceDb(schoolDb, xxStringHasher)
    }
    override val onRoasterDataSource: OneRosterDataSourceLocal by lazy {
        OneRosterDataSourceDb(schoolDb, xxStringHasher)
    }

}