package world.respect.datalayer

import world.respect.datalayer.school.AssignmentDataSourceLocal
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.PersonPasswordDataSourceLocal
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.SchoolAppDataSourceLocal

/**
 * Local DataSource implementation (eg based on a database). Local DataSources include putLocal
 * functions which are used to insert data loaded from a trusted upstream server without permission
 * checks (to run an offline-first cache).
 */
interface SchoolDataSourceLocal: SchoolDataSource {

    override val schoolAppDataSource: SchoolAppDataSourceLocal

    override val personDataSource: PersonDataSourceLocal

    override val personPasskeyDataSource: PersonPasskeyDataSourceLocal

    override val personPasswordDataSource: PersonPasswordDataSourceLocal

    override val reportDataSource: ReportDataSourceLocal

    override val classDataSource: ClassDataSourceLocal

    override val enrollmentDataSource: EnrollmentDataSourceLocal

    override val assignmentDataSource: AssignmentDataSourceLocal
}