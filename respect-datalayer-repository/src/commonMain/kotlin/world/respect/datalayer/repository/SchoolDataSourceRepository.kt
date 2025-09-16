package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.repository.school.writequeue.ClassDataSourceRepository
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.writequeue.RemoteWriteQueue

class SchoolDataSourceRepository(
    internal val local: SchoolDataSourceLocal,
    internal val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : SchoolDataSource {

    override val personDataSource: PersonDataSourceRepository by lazy {
        PersonDataSourceRepository(
            local.personDataSource,
            remote.personDataSource,
            validationHelper,
            remoteWriteQueue,
        )
    }

    override val reportDataSource: ReportDataSource by lazy {
        local.reportDataSource
    }

    override val indicatorDataSource: IndicatorDataSource by lazy {
        local.indicatorDataSource
    }

    override val classDataSource: ClassDataSourceRepository by lazy {
        ClassDataSourceRepository(
            local = local.classDataSource,
            remote = remote.classDataSource,
            validationHelper = validationHelper,
            remoteWriteQueue = remoteWriteQueue,
        )
    }

    override val enrollmentDataSource: EnrollmentDataSource by lazy {
        local.enrollmentDataSource
    }
}