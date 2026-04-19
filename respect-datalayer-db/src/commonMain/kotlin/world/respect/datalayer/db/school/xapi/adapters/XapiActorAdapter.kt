package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.ActorEntity
import world.respect.datalayer.db.school.xapi.entities.ActorEntityTypeEnum
import world.respect.datalayer.db.school.xapi.entities.GroupMemberActorJoin
import world.respect.datalayer.ext.EPOCH
import world.respect.datalayer.school.xapi.ext.idStr
import world.respect.datalayer.school.xapi.model.XapiAccount
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.school.xapi.model.isAnonymous
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


data class ActorEntities(
    val actor: ActorEntity,
    val groupMemberAgents: List<ActorEntity> = emptyList(),
    val groupMemberJoins: List<GroupMemberActorJoin> = emptyList(),
)



fun XapiActor.identifierHash(uidNumberMapper: UidNumberMapper): Long {
    return idStr?.let { uidNumberMapper(it) } ?: 0
}


fun XapiActor.toEntities(
    uidNumberMapper: UidNumberMapper,
    lastModified: Instant,
): ActorEntities {
    return when(this) {
        is XapiAgent -> ActorEntities(
            toActorEntity(uidNumberMapper, lastModified)
        )
        is XapiGroup -> toGroupEntities(
            uidNumberMapper, lastModified
        )
    }
}

fun XapiAgent.toActorEntity(
    uidNumberMapper: UidNumberMapper,
    lastModified: Instant,
) : ActorEntity {
    val uid = identifierHash(uidNumberMapper)
    return ActorEntity(
        actorUid = uid,
        actorName = name,
        actorPersonUid = 0L,
        actorMbox = mbox,
        actorMbox_sha1sum = mbox_sha1sum,
        actorOpenid = openid,
        actorAccountName = account?.name,
        actorAccountHomePage = account?.homePage,
        actorObjectType = ActorEntityTypeEnum.AGENT,
        actorLastModified = lastModified,
    )
}

/**
 * If Group is anonymous:
 *   The group will never be modified. The Actor.actorUid for the ActorEntity representing the group
 *   itself will be created by door primary key manager. All GroupMemberActorJoins will be new.
 *   As per the Xapi Spec :
 *   https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *   "A Learning Record Consumer MUST consider each Anonymous Group distinct even if it has an
 *   identical set of members."
 *
 * If the group is identified:
 *   The group members might be updated. The Actor.actorUid uses the identifierHash (e.g. the same
 *   as an Agent). AccountEtag will be a hash of hashes of the member agents.
 *
 *   The GroupMemberActorJoins will only change IF the AccountEtag was changed (e.g. the an
 *   ActorEntity representing an identified group has a AccountEtag to the already existing one,
 *   then GroupMemberActorJoins need to be inserted if not already existing, or the last modified
 *   time needs to be updated and set to be the same as Actor.actorLct for the entity representing
 *   the group).
 *
 *   Matching the last modified times makes it possible to query the current members of the group
 *   by matching the ActorEntity.actorLct and GroupMemberActorJoin.gmajLastMod.
 */
fun XapiGroup.toGroupEntities(
    uidNumberMapper: UidNumberMapper,
    lastModified: Instant,
) : ActorEntities {

    val memberActorEntities = member?.map {
        it.toActorEntity(
            uidNumberMapper = uidNumberMapper,
            lastModified = lastModified,
        )
    } ?: emptyList()

    val groupActor = ActorEntity(
        actorUid = if(isAnonymous) {
            uidNumberMapper(Uuid.random().toString())
        }else {
            identifierHash(uidNumberMapper)
        },
        actorObjectType = ActorEntityTypeEnum.GROUP,
        actorName = name,
        actorMbox = mbox,
        actorMbox_sha1sum = mbox_sha1sum,
        actorOpenid = openid,
        actorAccountName = account?.name,
        actorAccountHomePage = account?.homePage,
        actorLastModified = lastModified,
    )

    return ActorEntities(
        actor = groupActor,
        groupMemberAgents = memberActorEntities,
        groupMemberJoins = memberActorEntities.mapIndexed { index, memberActorEntity ->
            GroupMemberActorJoin(
                gmajGroupActorUid = groupActor.actorUid,
                gmajMemberActorUid = memberActorEntity.actorUid,
            )
        }
    )
}

fun ActorEntity.toAgentModel(): XapiAgent {
    return XapiAgent(
        name = actorName,
        mbox = actorMbox,
        mbox_sha1sum = actorMbox_sha1sum,
        openid = actorOpenid,
        account = XapiAccount.fromHomePageAndNameOrNull(actorAccountHomePage, actorAccountName),
        objectType = XapiObjectType.Agent,
    )
}

fun ActorEntities.toGroupModel(): XapiGroup {
    return XapiGroup(
        name = actor.actorName,
        mbox = actor.actorMbox,
        mbox_sha1sum = actor.actorMbox_sha1sum,
        openid = actor.actorOpenid,
        objectType = XapiObjectType.Group,
        account = XapiAccount.fromHomePageAndNameOrNull(
            actor.actorAccountHomePage, actor.actorAccountName
        ),
        member = groupMemberJoins.mapNotNull { groupMemberJoin ->
            groupMemberAgents.firstOrNull {
                it.actorUid == groupMemberJoin.gmajMemberActorUid
            }?.toAgentModel()
        }
    )
}


fun ActorEntities.toModel(): XapiActor {
    return when(actor.actorObjectType) {
        ActorEntityTypeEnum.AGENT -> {
            actor.toAgentModel()
        }

        ActorEntityTypeEnum.GROUP -> {
            this.toGroupModel()
        }

    }
}

/**
 * An identified group may omit the member property. We need keep only one ActorEntities object per
 * unique actor, and when that is a group, it must have all members identified (if any).
 *
 * Otherwise, there is a risk that the same identified group is in a statement in multiple different
 * places (e.g. the statement actor and team), and then the identified group with no members could
 * override the identified group with the members, and members information would be lost.
 */
fun List<ActorEntities>.flattenActors(): List<ActorEntities> {
    return map { it.actor.actorUid }.distinct().mapNotNull { actorUid ->
        val allByUid = this.filter { it.actor.actorUid == actorUid }
        allByUid.firstOrNull()?.let { first ->
            ActorEntities(
                actor = first.actor,
                groupMemberAgents = this.flatMap { it.groupMemberAgents }.distinctBy { it.actorUid },
                groupMemberJoins = this.flatMap { join ->
                    join.groupMemberJoins.distinctBy { it.gmajMemberActorUid }
                }
            )
        }
    }
}
