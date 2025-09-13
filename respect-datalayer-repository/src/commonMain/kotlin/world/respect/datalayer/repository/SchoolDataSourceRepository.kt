package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.writequeue.RemoteWriteQueue

class SchoolDataSourceRepository(
    private val local: SchoolDataSourceLocal,
    private val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
    private val uidNumberMapper: UidNumberMapper,
) : SchoolDataSource {

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceRepository(
            local.personDataSource,
            remote.personDataSource,
            validationHelper,
            remoteWriteQueue,
            uidNumberMapper = uidNumberMapper,
        )
    }
    override val reportDataSource: ReportDataSource by lazy {
        local.reportDataSource
    }
    override val indicatorDataSource: IndicatorDataSource by lazy {
        local.indicatorDataSource
    }

    override val classDataSource: ClassDataSource by lazy {
        local.classDataSource
    }

    override val enrollmentDataSource by lazy {
        local.enrollmentDataSource
    }
}