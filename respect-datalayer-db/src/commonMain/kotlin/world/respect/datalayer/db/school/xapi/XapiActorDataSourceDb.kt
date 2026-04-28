package world.respect.datalayer.db.school.xapi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.ActorEntities
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.adapters.toModel
import world.respect.datalayer.ext.EPOCH
import world.respect.datalayer.school.xapi.XapiActorDataSourceLocal
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiGroup
import kotlin.time.Instant

class XapiActorDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val uidNumberMapper: UidNumberMapper,
) : XapiActorDataSourceLocal{

    override suspend fun updateLocal(
        actors: List<XapiActor>,
        timestamp: Instant,
    ) {

        actors.forEach { actor ->
            val entities = actor.toEntities(
                uidNumberMapper = uidNumberMapper,
                lastModified = timestamp,
            )

            val allActorEntities = (listOf(entities.actor) + entities.groupMemberAgents)

            val updateTime = timestamp.toEpochMilliseconds()

            if(actor is XapiGroup) {
                val members = actor.member

                //An anonymous group will always have a new uid, will never be in db
                val existingEntityInDb = schoolDb.getActorDao().takeIf { !actor.isAnonymous }
                    ?.findByUidAsync(entities.actor.actorUid)

                val updateMembers = members != null &&
                    (timestamp > (existingEntityInDb?.actorGroupMembersLastUpdated ?: EPOCH))

                if(updateMembers) {
                    //If this is an identified group, we need to delete previous group member joins
                    schoolDb.takeIf { !actor.isAnonymous }?.getGroupMemberActorJoinDao()
                        ?.deleteByGroupActorUidAsync(entities.actor.actorUid)

                    schoolDb.getGroupMemberActorJoinDao().insertOrIgnoreListAsync(
                        entities.groupMemberJoins
                    )

                    schoolDb.getActorDao().updateGroupMembersLastUpdated(
                        actorUid = entities.actor.actorUid,
                        updateTime = updateTime
                    )
                }
            }

            //The name property is the only property that can change
            allActorEntities.forEach {
                schoolDb.getActorDao().updateIfNameChanged(
                    uid = it.actorUid,
                    name = it.actorName,
                    updateTime = updateTime
                )
            }
            schoolDb.getActorDao().insertOrIgnoreListAsync(allActorEntities)

        }
    }

    override suspend fun getPerson(
        actor: XapiActor,
        dataLoadParams: DataLoadParams
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupDetail(groupId: String): XapiGroup? {
        // Find the group actor by its accountName (groupId)
        val groupActorEntity = schoolDb.getActorDao().findGroupByAccountNameAsync(groupId)
            ?: return null

        // Find all group member joins for this group
        val groupMemberJoins = schoolDb.getGroupMemberActorJoinDao()
            .findByGroupActorUidList(listOf(groupActorEntity.actorUid))

        // Find all member actor entities
        val memberActorUids = groupMemberJoins.map { it.gmajMemberActorUid }
        val memberActorEntities = if (memberActorUids.isNotEmpty()) {
            schoolDb.getActorDao().findByUidList(memberActorUids)
        } else {
            emptyList()
        }

        // Convert to model using adapter
        val actorEntities = ActorEntities(
            actor = groupActorEntity,
            groupMemberAgents = memberActorEntities,
            groupMemberJoins = groupMemberJoins
        )

        return actorEntities.toModel() as? XapiGroup
    }

    override suspend fun getGroupsByIds(groupIds: List<String>): List<XapiGroup> {
        if (groupIds.isEmpty()) return emptyList()

        return groupIds.mapNotNull { groupId ->
            getGroupDetail(groupId)
        }
    }

    override fun getGroupDetailAsFlow(groupId: String): Flow<XapiGroup?> = flow {
        emit(getGroupDetail(groupId))
    }
}