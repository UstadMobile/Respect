package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import kotlin.time.Instant

@Serializable
data class Bookmark(
    val uid: String,
    val status: StatusEnum = StatusEnum.ACTIVE,
    override val lastModified: Instant,
    override val stored: Instant,
    val personUid: String,
    val learningUnitManifestUrl: String,
    val title: String? = null,
    val subtitle: String? = null,
    val appIcon: String,
    val appName: String,
    val iconUrl: String? = null,
    val appManifestUrl: String,
    val expectedIdentifier: String,
    val refererUrl: String,
) : ModelWithTimes