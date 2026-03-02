package world.respect.datalayer.db.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey val urlHash: String,
    val title: String?,
    val subtitle: String?,
    val appIcon: String,
    val appName: String,
    val iconUrl: String?,
    val appManifestUrl: String,
    val expectedIdentifier: String?,
    val refererUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)