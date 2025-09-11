package world.respect.datalayer

import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.ReportDataSourceLocal

/**
 * Local DataSource implementation (eg based on a database). Local DataSources include putLocal
 * functions which are used to insert data loaded from a trusted upstream server without permission
 * checks (to run an offline-first cache).
 */
interface SchoolDataSourceLocal: SchoolDataSource {

    override val personDataSource: PersonDataSourceLocal

    override val reportDataSource: ReportDataSourceLocal

}