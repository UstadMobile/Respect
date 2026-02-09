package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.opds.OpdsDataSourceRepository
import world.respect.datalayer.repository.school.AssignmentDataSourceRepository
import world.respect.datalayer.repository.school.ClassDataSourceRepository
import world.respect.datalayer.repository.school.EnrollmentDataSourceRepository
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.repository.school.InviteDataSourceRepository
import world.respect.datalayer.repository.school.PersonPasskeyDataSourceRepository
import world.respect.datalayer.repository.school.PersonPasswordDataSourceRepository
import world.respect.datalayer.repository.school.PersonQrCodeBadgeDataSourceRepository
import world.respect.datalayer.repository.school.SchoolAppDataSourceRepository
import world.respect.datalayer.repository.school.SchoolPermissionGrantDataSourceRepository
import world.respect.datalayer.school.DummySchoolConfigSettingsDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.opds.OpdsDataSource
import world.respect.datalayer.school.writequeue.RemoteWriteQueue

class SchoolDataSourceRepository(
    internal val local: SchoolDataSourceLocal,
    internal val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : SchoolDataSource {

    override val reportDataSource: ReportDataSource by lazy {
        local.reportDataSource
    }

    override val indicatorDataSource: IndicatorDataSource by lazy {
        local.indicatorDataSource
    }

    override val schoolAppDataSource: SchoolAppDataSourceRepository by lazy {
        SchoolAppDataSourceRepository(
            local = local.schoolAppDataSource,
            remote = remote.schoolAppDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val schoolPermissionGrantDataSource: SchoolPermissionGrantDataSourceRepository by lazy {
        SchoolPermissionGrantDataSourceRepository(
            local = local.schoolPermissionGrantDataSource,
            remote = remote.schoolPermissionGrantDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val classDataSource: ClassDataSourceRepository by lazy {
        ClassDataSourceRepository(
            local = local.classDataSource,
            remote = remote.classDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val enrollmentDataSource: EnrollmentDataSourceRepository by lazy {
        EnrollmentDataSourceRepository(
            local = local.enrollmentDataSource,
            remote = remote.enrollmentDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val personDataSource: PersonDataSourceRepository by lazy {
        PersonDataSourceRepository(
            local.personDataSource,
            remote.personDataSource,
            validationHelper,
            remoteWriteQueue
        )
    }

    override val personPasswordDataSource: PersonPasswordDataSourceRepository by lazy {
        PersonPasswordDataSourceRepository(
            local = local.personPasswordDataSource,
            remote = remote.personPasswordDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }
    override val personQrBadgeDataSource: PersonQrCodeBadgeDataSourceRepository by lazy {
        PersonQrCodeBadgeDataSourceRepository(
            local = local.personQrBadgeDataSource,
            remote = remote.personQrBadgeDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }
    override val personPasskeyDataSource: PersonPasskeyDataSource by lazy {
        PersonPasskeyDataSourceRepository(
            local = local.personPasskeyDataSource,
            remote = remote.personPasskeyDataSource,
            validationHelper = validationHelper
        )
    }

    override val assignmentDataSource: AssignmentDataSourceRepository by lazy {
        AssignmentDataSourceRepository(
            local = local.assignmentDataSource,
            remote = remote.assignmentDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val inviteDataSource: InviteDataSourceRepository by lazy {
        InviteDataSourceRepository(
            local = local.inviteDataSource,
            remote = remote.inviteDataSource,
            remoteWriteQueue = remoteWriteQueue,
            validationHelper = validationHelper
        )
    }

    override val opdsDataSource: OpdsDataSource by lazy {
        OpdsDataSourceRepository(
            local = local.opdsDataSource,
            remote = remote.opdsDataSource,
        )
    }

    override val schoolConfigSettingDataSource: SchoolConfigSettingDataSource by lazy {
        DummySchoolConfigSettingsDataSource()
    }
}