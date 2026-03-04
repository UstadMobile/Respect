package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.StatusEnum

@Entity(
    primaryKeys = ["bPersonUidNum", "bUrlHash"]
)
data class BookmarkEntity(
    val bUrlHash:Long,
    val bPersonUid: String,
    val bPersonUidNum: Long,
    val bLearningUnitManifestUrl:String,

    val bTitle: String?,
    val bSubtitle: String?,
    val bAppIcon: String,
    val bAppName: String,
    val bIconUrl: String?,
    val bAppManifestUrl: String,
    val bExpectedIdentifier: String,
    val bRefererUrl: String,
    val bStatus: Int = StatusEnum.ACTIVE.flag,
    val bCreatedAt: Long = System.currentTimeMillis(),
    val bUpdatedAt: Long = System.currentTimeMillis()
)