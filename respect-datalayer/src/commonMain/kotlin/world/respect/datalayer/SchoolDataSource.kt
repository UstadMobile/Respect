package world.respect.datalayer

import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.PersonQrDataSource
import world.respect.datalayer.school.SchoolAppDataSource

/**
 * DataSource for data which is specific to a given School and authenticated user (see
 * ARCHITECTURE.md for more info).
 *
 * The DataSource requires a user guid and (for a network client) an authorization token.
 */
interface SchoolDataSource {

    val schoolAppDataSource: SchoolAppDataSource

    val personDataSource: PersonDataSource

    val personPasskeyDataSource: PersonPasskeyDataSource

    val personPasswordDataSource: PersonPasswordDataSource

    val personQrDataSource: PersonQrDataSource

    val reportDataSource: ReportDataSource

    val indicatorDataSource: IndicatorDataSource

    val classDataSource: ClassDataSource

    val enrollmentDataSource: EnrollmentDataSource

    val assignmentDataSource: AssignmentDataSource

}