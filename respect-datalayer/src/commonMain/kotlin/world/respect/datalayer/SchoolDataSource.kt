package world.respect.datalayer

import world.respect.datalayer.school.opds.OpdsDataSource
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.PersonQrBadgeDataSource
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.SchoolPermissionGrantDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSource

/**
 * DataSource for data which is specific to a given School and authenticated user (see
 * ARCHITECTURE.md for more info).
 *
 * The DataSource requires a user guid and (for a network client) an authorization token.
 */
interface SchoolDataSource {

    val schoolAppDataSource: SchoolAppDataSource

    val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSource

    val personDataSource: PersonDataSource

    val personPasskeyDataSource: PersonPasskeyDataSource

    val personPasswordDataSource: PersonPasswordDataSource

    val personQrBadgeDataSource: PersonQrBadgeDataSource

    val reportDataSource: ReportDataSource

    val indicatorDataSource: IndicatorDataSource

    val classDataSource: ClassDataSource

    val enrollmentDataSource: EnrollmentDataSource

    val assignmentDataSource: AssignmentDataSource

    val inviteDataSource: InviteDataSource

    val opdsDataSource: OpdsDataSource

    val opdsFeedDataSource: OpdsFeedDataSource

    val schoolConfigSettingDataSource: SchoolConfigSettingDataSource

}