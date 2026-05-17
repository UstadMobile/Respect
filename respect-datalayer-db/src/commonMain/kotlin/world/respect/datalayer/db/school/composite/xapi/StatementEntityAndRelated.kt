package world.respect.datalayer.db.school.composite.xapi

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.school.entities.xapi.ActorEntity
import world.respect.datalayer.db.school.entities.xapi.GroupMemberActorJoin
import world.respect.datalayer.db.school.entities.xapi.StatementEntity

@Serializable
class StatementEntityAndRelated(
    @Embedded
    var statementEntity: StatementEntity? = null,

    @Embedded
    var groupMemberActorJoin: GroupMemberActorJoin? = null,

    @Embedded
    var actorEntity: ActorEntity? = null,
)

