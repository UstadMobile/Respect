package world.respect.datalayer.repository

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSource

class SchoolDataSourceRepository(
    private val local: SchoolDataSourceLocal,
    private val remote: SchoolDataSource,
) : SchoolDataSource {

    override val personDataSource: PersonDataSource
        get() = TODO("Not yet implemented")
    override val reportDataSource: ReportDataSource
        get() = TODO("Not yet implemented")
    override val indicatorDataSource: IndicatorDataSource
        get() = TODO("Not yet implemented")
}