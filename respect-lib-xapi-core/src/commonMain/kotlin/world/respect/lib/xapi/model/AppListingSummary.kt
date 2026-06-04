package world.respect.lib.xapi.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlin.time.Instant

/**
 * A summary of an app for list display from xAPI app listing recipe.
 */
@Serializable
data class AppListingSummary(
    val activityId: String,
    val title: String,
    val description: String = "",
    val appManifestUrl: Url,
    val moreInfoUrl: Url? = null,
    @Contextual
    val lastModified: Instant,
)


