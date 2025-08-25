package world.respect.datalayer.respect.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.datalayer.opds.model.LangMap

/**
 * A RESPECT Realm (a logical grouping of networked resources), each with its own users, usage data,
 * and apps. This is typically a school. Each Realm has its own xAPI and OneRoster URLs, and may
 * also have a RESPECT API server.
 *
 * @property name the name of the realm (e.g. school name), potentially in more than one language
 * @property self the absolute URL to this Respect Realm, under which . https://school.example.org/ .
 *           The JSON should be available at https://school.example.org/.well-known/respect-realm.json
 * @property xapi URL to xAPI endpoint e.g. https://school.example.org/api/xapi/
 * @property oneRoster URL to OneRoster endpoint e.g. https://school.example.org/api/oneroster/
 * @property respectExt URL to Respect extensions endpoint (if available). Required for invites etc
 *           e.g. https://school.example.org/api/respect/.
 * @property rpId the domain for signup and login with passkey
 *
 */
@Serializable
data class RespectRealm(
    val name: LangMap,
    val self: Url,
    val xapi: Url,
    val oneRoster: Url,
    val respectExt: Url?,
    val rpId: String?,
)
