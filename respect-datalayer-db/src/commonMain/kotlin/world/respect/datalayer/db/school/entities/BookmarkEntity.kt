package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
data class BookmarkEntity(

    val bUid: String,

    @PrimaryKey
    val bUidNum: Long,

    val bStatus: StatusEnum = StatusEnum.ACTIVE,

    val bLastModified: Instant = Clock.System.now(),

    val bStored: Instant = Clock.System.now(),

    val bPersonUid: String,
    val bPersonUidNum: Long,

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