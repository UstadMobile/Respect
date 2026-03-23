package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.ActorEntity


/**
 * Shorthand to differentiate between an anonymous (e.g. group) and an identified group
 * as per the xAPI spec.
 */
fun ActorEntity.isAnonymous(): Boolean {
    return (actorOpenid == null && actorMbox == null && actorAccountName == null && actorMbox_sha1sum == null)
}
