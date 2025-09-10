package world.respect.shared.domain.school

import world.respect.datalayer.school.model.Person
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.school.model.Report

/**
 * Wrapper class used only for purposes of differentiating it for dependency injection purposes
 */
data class SchoolPrimaryKeyGenerator(
    val primaryKeyGenerator: PrimaryKeyGenerator
) {
    companion object {

        val TABLE_IDS = listOf(Person.TABLE_ID, Report.TABLE_ID, Indicator.TABLE_ID)

    }
}