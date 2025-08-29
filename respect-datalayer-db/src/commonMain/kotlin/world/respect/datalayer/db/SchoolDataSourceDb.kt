package world.respect.datalayer.db

import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.school.IndicatorDataSourceDb
import world.respect.datalayer.db.school.PersonDataSourceDb
import world.respect.datalayer.db.school.ReportDataSourceDb
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.libxxhash.XXStringHasher

class SchoolDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher,
) : SchoolDataSourceLocal{

    override val personDataSource: PersonDataSourceLocal by lazy {
        PersonDataSourceDb(schoolDb, xxStringHasher)
    }
    override val reportDataSource: ReportDataSource
        get() = ReportDataSourceDb(schoolDb)

    override val indicatorDataSource: IndicatorDataSource
        get() = IndicatorDataSourceDb(schoolDb)

}