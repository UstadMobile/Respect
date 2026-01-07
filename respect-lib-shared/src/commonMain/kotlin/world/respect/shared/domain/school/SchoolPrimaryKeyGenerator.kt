package world.respect.shared.domain.school

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.opds.entities.OpdsFacetEntity
import world.respect.datalayer.db.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.opds.entities.OpdsGroupEntity
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.Enrollment
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.school.model.Report

/**
 * Wrapper class used only for purposes of differentiating it for dependency injection purposes
 */
data class SchoolPrimaryKeyGenerator(
    val primaryKeyGenerator: PrimaryKeyGenerator = PrimaryKeyGenerator(TABLE_IDS)
) {
    companion object {

        val TABLE_IDS = RespectSchoolDatabase.TABLE_IDS

    }
}