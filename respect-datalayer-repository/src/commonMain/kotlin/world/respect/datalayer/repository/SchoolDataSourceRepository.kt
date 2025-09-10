package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.school.PersonDataSourceRepository
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
    override val reportDataSource: ReportDataSource
        get() = TODO("Not yet implemented")
    override val indicatorDataSource: IndicatorDataSource
        get() = TODO("Not yet implemented")

}