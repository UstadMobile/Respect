package world.respect.datasource.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datasource.db.opds.entities.OpdsFeedMetadataEntity

@Dao
abstract class OpdsFeedMetadataEntityDao {

    @Query("""
        SELECT OpdsFeedMetadataEntity.*
          FROM OpdsFeedMetadataEntity
         WHERE ofmeOfeUid = :feedUid 
    """)
    abstract suspend fun findByFeedUid(feedUid: Long): List<OpdsFeedMetadataEntity>

    @Query("""
        DELETE FROM OpdsFeedMetadataEntity 
         WHERE ofmeOfeUid = :feedUid
    """)
    abstract suspend fun deleteByFeedUid(feedUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<OpdsFeedMetadataEntity>)

}