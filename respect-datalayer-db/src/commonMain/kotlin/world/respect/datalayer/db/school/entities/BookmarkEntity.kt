package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.StatusEnum

@Entity
data class BookmarkEntity(
    @PrimaryKey val urlHash: Long,
    val learningUnitUrl:String,
    val title: String?,
    val subtitle: String?,
    val appIcon: String,
    val appName: String,
    val iconUrl: String?,
    val appManifestUrl: String,
    val expectedIdentifier: String,
    val refererUrl: String,
    val status: Int = StatusEnum.ACTIVE.flag,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)