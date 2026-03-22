package world.respect.datalayer.db.school.composite.xapi

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.school.xapi.entities.ActivityEntity
import world.respect.datalayer.db.school.xapi.entities.StatementEntity

@Serializable
data class StatementAndActivity(
    @Embedded
    var statementEntity: StatementEntity = StatementEntity(),
    @Embedded
    var activityEntity: ActivityEntity? = null
)