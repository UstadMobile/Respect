package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.school.IndicatorDataSourceRepository
import world.respect.datalayer.repository.school.PersonDataSourceRepository
import world.respect.datalayer.repository.school.ReportDataSourceRepository
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ReportDataSource

class SchoolDataSourceRepository(
    private val local: SchoolDataSourceLocal,
    private val remote: SchoolDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
) : SchoolDataSource {

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceRepository(
            local.personDataSource, remote.personDataSource, validationHelper
        )
    }
    override val reportDataSource: ReportDataSource by lazy {
        ReportDataSourceRepository(
            local.reportDataSource, remote.reportDataSource
        )
    }
    override val indicatorDataSource: IndicatorDataSource by lazy {
        IndicatorDataSourceRepository(
            local.indicatorDataSource, remote.indicatorDataSource
        )
    }

    override val classDataSource: ClassDataSource by lazy {
        local.classDataSource
    }
    override val enrollmentDataSource: EnrollmentDataSource by lazy {
        local.enrollmentDataSource
    }
}