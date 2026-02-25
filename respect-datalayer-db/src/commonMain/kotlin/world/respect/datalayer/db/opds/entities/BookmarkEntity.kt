package world.respect.datalayer.db.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity(
    @PrimaryKey val urlHash: Long,
    val isBookmarked: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)