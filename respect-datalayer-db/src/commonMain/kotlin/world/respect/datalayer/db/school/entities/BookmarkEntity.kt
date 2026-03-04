package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity(
    primaryKeys = ["bPersonUid", "bLearningUnitManifestUrl"]
)
data class BookmarkEntity(

    val bStatus: StatusEnum = StatusEnum.ACTIVE,

    val bLastModified: Instant,

    val bStored: Instant,

    val bPersonUid: String?=null,

    val bLearningUnitManifestUrl: String,

    val bTitle: String? = null,
    val bSubtitle: String? = null,

    val bAppIcon: String,
    val bAppName: String,
    val bIconUrl: String? = null,

    val bAppManifestUrl: String,
    val bExpectedIdentifier: String,
    val bRefererUrl: String,
)