package world.respect.domain.respectappmanifest.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

/**
 * @param name the name of the site e.g. name of the School
 * @param description a description of the site
 * @param url the URL for this site (e.g. self)
 * @param xapiUrl xAPI LRS URL for this site
 * @param oneRosterUrl oneRoster URL for this site
 * @param appLists a list of URLs, each of which is either a RespectAppManifest JSON or a list
 *        thereof. These apps would generally be shown to the admin such that they can select apps
 *        for teachers and students to use.
 */
@Suppress("unused") //Reserved for future use
@Serializable
data class RespectSite(
    val name: String,
    val description: String,
    val url: Url,
    val xapiUrl: Url,
    val oneRosterUrl: Url,
    val appLists: List<Url>,
)
