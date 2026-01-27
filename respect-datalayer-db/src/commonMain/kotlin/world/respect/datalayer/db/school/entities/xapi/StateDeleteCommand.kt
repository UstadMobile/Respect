package world.respect.datalayer.db.school.entities.xapi

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * Xapi Delete commands can require the deletion of all state ids for a particular context (agent +
 * activityId). If the device is offline, and other state ids for the context exist, then this is a
 * problem.
 *
 * A trigger will fire when StateDeleteCommand is inserted or updated to action the delete command,
 * e.g. when a delete all state ids for context occurs offline, then the delete command will be
 * sent upstream when a connection is next available (as usual), and when it reaches the server the
 * trigger will delete any applicable StateEntity(s) on the upstream server.
 *
 * See DeleteXapiStateUseCase for details.
 *
 * @param sdcActorUid the actorUid for the state
 * @param sdcHash - hash of other keys that are part of the identifier - activityId,
 *        registrationUuid (if included), stateId (if included)
 *
 */
@Entity(
    primaryKeys = ["sdcActorUid", "sdcHash"]
)
@Serializable
data class StateDeleteCommand(
    var sdcActorUid: Long = 0,

    var sdcHash: Long = 0,

    var sdcActivityUid: Long = 0,

    var sdcStateId: String? = null,

    var sdcLastMod: Long = 0,

    var sdcRegistrationHi: Long? = null,

    var sdcRegistrationLo: Long? = null,
) {
    companion object {
        const val TABLE_ID = 121422
    }
}