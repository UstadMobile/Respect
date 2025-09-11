package world.respect.datalayer.db

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.oneroaster.OneRosterDataSourceDb
import world.respect.datalayer.db.school.ClassDatasourceDb
import world.respect.datalayer.db.school.IndicatorDataSourceDb
import world.respect.datalayer.db.school.PersonDataSourceDb
import world.respect.datalayer.oneroster.OneRosterDataSourceLocal
import world.respect.datalayer.db.school.ReportDataSourceDb
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.libxxhash.XXStringHasher

/**
 * SchoolDataSource implementation based on a local (Room) database
 *
 * @property schoolDb the school database
 * @property xxStringHasher xx hasher
 * @property authenticatedUser the authenticated user. The DataSource will use this to carry out
 *           permission checks as required, except when using putLocal functions (which are used by
 *           the repository to cache data from upstream).
 */
class SchoolDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : SchoolDataSourceLocal{

    override val personDataSource: PersonDataSourceLocal by lazy {
        PersonDataSourceDb(schoolDb, xxStringHasher, authenticatedUser)
    }
    override val onRoasterDataSource: OneRosterDataSourceLocal by lazy {
        OneRosterDataSourceDb(schoolDb, xxStringHasher)
    }
    override val reportDataSource: ReportDataSourceLocal by lazy {
        ReportDataSourceDb(schoolDb)
    }

    override val indicatorDataSource: IndicatorDataSource by lazy {
        IndicatorDataSourceDb(schoolDb)
    }

    override val classDataSource: ClassDataSourceLocal by lazy {
        ClassDatasourceDb(schoolDb, xxStringHasher, authenticatedUser)
    }
}