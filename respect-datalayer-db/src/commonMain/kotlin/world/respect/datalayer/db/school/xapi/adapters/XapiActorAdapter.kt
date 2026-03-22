package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.ActorEntity
import world.respect.datalayer.db.school.xapi.entities.GroupMemberActorJoin
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.isAnonymous
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.util.time.systemTimeInMillis


data class ActorEntities(
    val actor: ActorEntity,
    val groupMemberAgents: List<ActorEntity> = emptyList(),
    val groupMemberJoins: List<GroupMemberActorJoin> = emptyList(),
)

fun XapiActor.identifierHash(uidNumberMapper: UidNumberMapper): Long {
    val idStr = when {
        account != null -> "${account?.name}@${account?.homePage}"
        mbox != null -> mbox
        mbox_sha1sum != null -> mbox_sha1sum
        openid != null -> openid
        else -> null
    }

    return idStr?.let { uidNumberMapper(it) } ?: 0
}


fun XapiActor.toEntities(
    uidNumberMapper: UidNumberMapper,
    primaryKeyGenerator: PrimaryKeyGenerator,
): ActorEntities {
    return when(this) {
        is XapiAgent -> ActorEntities(toActorEntity(uidNumberMapper))
        is XapiGroup -> toGroupEntities(
            uidNumberMapper, primaryKeyGenerator
        )
    }
}

fun XapiAgent.toActorEntity(
    uidNumberMapper: UidNumberMapper,
) : ActorEntity {
    val uid = identifierHash(uidNumberMapper)
    return ActorEntity(
        actorUid = uid,
        actorPersonUid = 0L,
        actorMbox = mbox,
        actorMbox_sha1sum = mbox_sha1sum,
        actorOpenid = openid,
        actorAccountName = account?.name,
        actorAccountHomePage = account?.homePage,
        actorObjectType = XapiEntityObjectTypeFlags.AGENT,
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
    primaryKeyGenerator: PrimaryKeyGenerator,
) : ActorEntities {
    val modTime = systemTimeInMillis()

    val memberActorEntities = member.map {
        it.toActorEntity(uidNumberMapper)
    }

    val groupActor = ActorEntity(
        actorUid = if(isAnonymous) {
            primaryKeyGenerator.nextId(ActorEntity.TABLE_ID)
        }else {
            identifierHash(uidNumberMapper)
        },
        actorObjectType = XapiEntityObjectTypeFlags.GROUP,
        actorName = name,
        actorMbox = mbox,
        actorMbox_sha1sum = mbox_sha1sum,
        actorOpenid = openid,
        actorAccountName = account?.name,
        actorAccountHomePage = account?.homePage,
    )

    return ActorEntities(
        actor = groupActor,
        groupMemberAgents = memberActorEntities,
        groupMemberJoins = memberActorEntities.mapIndexed { index, memberActorEntity ->
            GroupMemberActorJoin(
                gmajGroupActorUid = groupActor.actorUid,
                gmajMemberActorUid = memberActorEntity.actorUid,
                gmajIndex = index,
            )
        }
    )
}
