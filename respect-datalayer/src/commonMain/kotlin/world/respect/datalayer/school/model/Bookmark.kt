package world.respect.datalayer.school.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.opds.model.LangMap
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

@Serializable
data class Bookmark(
    val personUid: String,
    val learningUnitManifestUrl: Url,
    val status: StatusEnum = StatusEnum.ACTIVE,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val title: LangMap? = null,
    val subTitle: LangMap? = null,
    val imageUrl: String? = null
) : ModelWithTimes

