package world.respect.datalayer.db.school.composite.xapi

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.school.xapi.entities.ActorEntity
import world.respect.datalayer.db.school.xapi.entities.GroupMemberActorJoin
import world.respect.datalayer.db.school.xapi.entities.StatementEntity

@Serializable
class StatementEntityAndRelated(
    @Embedded
    var statementEntity: StatementEntity? = null,

    @Embedded
    var groupMemberActorJoin: GroupMemberActorJoin? = null,

    @Embedded
    var actorEntity: ActorEntity? = null,
)

