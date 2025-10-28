package world.respect.datalayer.respect.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.opds.model.LangMap
import world.respect.lib.serializers.InstantAsISO8601

/**
 * A RESPECT school endpoint (a logical grouping of networked resources), each with its own users,
 * usage data, and apps. This is typically a single school. Each has its own xAPI and OneRoster URLs,
 * and may also have a RESPECT API server.
 *
 * @property name the name of the school potentially in more than one language
 * @property self the absolute URL to this Respect school, under which . https://school.example.org/ .
 *           The JSON should be available at https://school.example.org/.well-known/respect-school.json
 * @property xapi URL to xAPI endpoint e.g. https://school.example.org/api/school/xapi/
 * @property oneRoster URL to OneRoster endpoint e.g. https://school.example.org/api/school/oneroster/
 * @property respectExt URL to Respect extensions endpoint (if available). Required for invites etc
 *           e.g. https://school.example.org/api/school/respect/.
 */
@Serializable
data class SchoolDirectoryEntry(
    val name: LangMap,
    val self: Url,
    val xapi: Url,
    val oneRoster: Url,
    val respectExt: Url?,
    val rpId : String?,
    override val lastModified: InstantAsISO8601,
    override val stored: InstantAsISO8601,
): ModelWithTimes
