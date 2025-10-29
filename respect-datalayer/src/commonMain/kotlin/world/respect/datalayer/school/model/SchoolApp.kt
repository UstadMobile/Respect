package world.respect.datalayer.school.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * Represents an app added for a given school. This is currently little more than a reference to the
 * manifest URL,
 */
@Serializable
data class SchoolApp(
    val uid: String,
    val appManifestUrl: Url,
    val status: StatusEnum = StatusEnum.ACTIVE,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
) : ModelWithTimes {

    companion object {
        const val TABLE_ID = 19
    }
}
