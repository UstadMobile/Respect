package world.respect.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import world.respect.datalayer.db.school.xapi.adapters.ActivityEntities
import world.respect.datalayer.db.school.xapi.entities.StatementEntity

data class StatementDbEntities(
    @Embedded
    val statementEntity: StatementEntity,
    val activityEntities: ActivityEntities,
)
