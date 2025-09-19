package world.respect.datalayer.db

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.ClassDatasourceDb
import world.respect.datalayer.db.school.EnrollmentDataSourceDb
import world.respect.datalayer.db.school.IndicatorDataSourceDb
import world.respect.datalayer.db.school.PersonDataSourceDb
import world.respect.datalayer.db.school.ReportDataSourceDb
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.IndicatorDataSourceLocal
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.ReportDataSourceLocal

/**
 * SchoolDataSource implementation based on a local (Room) database
 *
 * @property schoolDb the school database
 * @property uidNumberMapper string uid to number mapper
 * @property authenticatedUser the authenticated user. The DataSource will use this to carry out
 *           permission checks as required, except when using putLocal functions (which are used by
 *           the repository to cache data from upstream).
 */
class SchoolDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : SchoolDataSourceLocal {

    override val personDataSource: PersonDataSourceLocal by lazy {
        PersonDataSourceDb(schoolDb, uidNumberMapper, authenticatedUser)
    }
    override val reportDataSource: ReportDataSourceLocal by lazy {
        ReportDataSourceDb(schoolDb, xxStringHasher)
    }

    override val indicatorDataSource: IndicatorDataSourceLocal by lazy {
        IndicatorDataSourceDb(schoolDb, xxStringHasher)
    }

    override val classDataSource: ClassDataSourceLocal by lazy {
        ClassDatasourceDb(schoolDb, uidNumberMapper, authenticatedUser)
    }

    override val enrollmentDataSource: EnrollmentDataSourceLocal by lazy {
        EnrollmentDataSourceDb(schoolDb, uidNumberMapper, authenticatedUser)
    }
}