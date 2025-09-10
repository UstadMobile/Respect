package world.respect.datalayer

import world.respect.datalayer.oneroster.OneRosterDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSource

/**
 * DataSource for data which is specific to a given Realm (eg school - see ARCHITECTURE.md for
 * more info).
 *
 * The DataSource requires a user guid and (for a network client) an authorization token.
 */
interface SchoolDataSource {

    val personDataSource: PersonDataSource

    val onRoasterDataSource: OneRosterDataSource

    val reportDataSource: ReportDataSource

    val indicatorDataSource: IndicatorDataSource

}