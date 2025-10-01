package world.respect.datalayer

import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource

/**
 * DataSource for data which is specific to a given School and authenticated user (see
 * ARCHITECTURE.md for more info).
 *
 * The DataSource requires a user guid and (for a network client) an authorization token.
 */
interface SchoolDataSource {
    val personDataSource: PersonDataSource

    val reportDataSource: ReportDataSource

    val indicatorDataSource: IndicatorDataSource

    val classDataSource: ClassDataSource

    val enrollmentDataSource: EnrollmentDataSource
}