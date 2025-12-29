package world.respect.datalayer.db.school.composite.xapi

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.school.entities.xapi.ActivityEntity
import world.respect.datalayer.db.school.entities.xapi.ActivityLangMapEntry
import world.respect.datalayer.db.school.entities.xapi.StatementEntity
import world.respect.datalayer.db.school.entities.xapi.VerbEntity
import world.respect.datalayer.db.school.entities.xapi.VerbLangMapEntry

@Serializable
class StatementEntityAndVerb(
    @Embedded
    var statementEntity: StatementEntity = StatementEntity(),
    @Embedded
    var verb: VerbEntity? = null,
    @Embedded
    var verbDisplay: VerbLangMapEntry? = null,
    @Embedded
    var activity: ActivityEntity? = null,
    @Embedded
    var activityLangMapEntry: ActivityLangMapEntry? = null,

    var statementActivityDescription: String? = null,
)

object StatementConst{
    const val SORT_BY_TIMESTAMP_DESC = 1

    const val SORT_BY_TIMESTAMP_ASC = 2

    const val SORT_BY_SCORE_DESC = 3

    const val SORT_BY_SCORE_ASC = 4

}