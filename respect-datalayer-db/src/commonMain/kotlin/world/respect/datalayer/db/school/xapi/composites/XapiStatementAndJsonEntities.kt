package world.respect.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import world.respect.datalayer.db.school.xapi.entities.XapiVerbEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityJson

data class XapiStatementAndJsonEntities(
    @Embedded
    val stmtEntity: XapiStatementEntity,
    @Embedded
    val verbEntity: XapiVerbEntity,
)