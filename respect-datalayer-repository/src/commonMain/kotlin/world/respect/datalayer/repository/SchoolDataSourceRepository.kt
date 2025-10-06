package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.school.ClassDataSourceRepository
import world.respect.datalayer.repository.school.EnrollmentDataSourceRepository
import world.respect.datalayer.repository.school.IndicatorDataSourceRepository
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.repository.school.ReportDataSourceRepository
import world.respect.datalayer.repository.school.PersonPasskeyDataSourceRepository
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.writequeue.RemoteWriteQueue

class SchoolDataSourceRepository(
    internal val local: SchoolDataSourceLocal,
    internal val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : SchoolDataSource {



    override val reportDataSource: ReportDataSourceRepository by lazy {
        ReportDataSourceRepository(
            local.reportDataSource,
            remote.reportDataSource,
            validationHelper,
            remoteWriteQueue,
        )
    }

    override val indicatorDataSource: IndicatorDataSourceRepository by lazy {
        IndicatorDataSourceRepository(
            local.indicatorDataSource,
            remote.indicatorDataSource,
            validationHelper,
            remoteWriteQueue,
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
            remoteWriteQueue,
            enrollmentDataSource
        )
    }

    override val personPasskeyDataSource: PersonPasskeyDataSource by lazy {
        PersonPasskeyDataSourceRepository(
            local = local.personPasskeyDataSource,
            remote = remote.personPasskeyDataSource,
            validationHelper = validationHelper
        )
    }

}