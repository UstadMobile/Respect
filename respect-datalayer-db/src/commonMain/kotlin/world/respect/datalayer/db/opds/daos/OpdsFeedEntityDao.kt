package world.respect.datalayer.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.shared.LastModifiedAndETagDb

@Dao
abstract class OpdsFeedEntityDao {

    @Query("""
        SELECT * 
          FROM OpdsFeedEntity 
         WHERE ofeUrlHash = :urlHash
    """)
    abstract fun findByUrlHashAsFlow(urlHash: Long): Flow<OpdsFeedEntity?>

    @Query("""
        DELETE FROM OpdsFeedEntity 
         WHERE ofeUid = :feedUid
    """)
    abstract suspend fun deleteByFeedUid(feedUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<OpdsFeedEntity>)

    @Query("""
        SELECT OpdsFeedEntity.ofeLastModifiedHeader AS lastModified,
               OpdsFeedEntity.ofeEtag AS etag
          FROM OpdsFeedEntity
         WHERE ofeUrlHash = :urlHash
    """)
    abstract suspend fun getLastModifiedAndETag(urlHash: Long): LastModifiedAndETagDb?


}