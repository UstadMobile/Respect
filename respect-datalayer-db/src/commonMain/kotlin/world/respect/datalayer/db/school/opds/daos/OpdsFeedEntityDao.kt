package world.respect.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.opds.entities.OpdsFeedEntity
import world.respect.datalayer.db.shared.LastModifiedAndETagDb
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Clock
import kotlin.time.Instant

@Dao
abstract class OpdsFeedEntityDao {

    @Query("""
        SELECT * 
          FROM OpdsFeedEntity 
         WHERE ofeUrlHash = :urlHash
    """)
    abstract fun findByUrlHashAsFlow(urlHash: Long): Flow<OpdsFeedEntity?>

    @Query("""
        SELECT * 
          FROM OpdsFeedEntity 
         WHERE ofeUrlHash = :urlHash
    """)
    abstract suspend fun findByUrlHash(urlHash: Long): OpdsFeedEntity?

    @Query("""
        SELECT OpdsFeedEntity.ofeLastModifiedHeader AS lastModified,
               OpdsFeedEntity.ofeEtag AS etag
          FROM OpdsFeedEntity
         WHERE OpdsFeedEntity.ofeUrlHash = :urlHash
     
    """)
    abstract suspend fun getNetworkValidationInfo(urlHash: Long): LastModifiedAndETagDb?


    @Query("""
        DELETE FROM OpdsFeedEntity 
         WHERE ofeUid = :feedUid
    """)
    abstract suspend fun deleteByFeedUid(feedUid: Long)

    @Query("""
        UPDATE OpdsFeedEntity 
           SET ofeStatus = :status,
               ofeLastModified = :lastModified
         WHERE ofeUid = :feedUid
    """)
    abstract suspend fun updateStatusByFeedUid(
        feedUid: Long,
        status: StatusEnum,
        lastModified: Instant = Clock.System.now()
    )

    @Insert
    abstract suspend fun insertList(entities: List<OpdsFeedEntity>)

    @Query(
        """
        SELECT OpdsFeedEntity.ofeLastModified AS lastModified,
               OpdsFeedEntity.ofeEtag AS etag
          FROM OpdsFeedEntity
         WHERE ofeUrlHash = :urlHash
    """
    )
    abstract suspend fun getLastModifiedAndETag(urlHash: Long): LastModifiedAndETagDb?

    @Query(
        """
        SELECT OpdsFeedEntity.*
          FROM OpdsFeedEntity
         WHERE ofeUrlHash IN (:urlHashes)
    """
    )
    abstract suspend fun findByUrlHashList(
        urlHashes: List<Long>
    ): List<OpdsFeedEntity>

    @Query(
        """
         SELECT OpdsFeedEntity.*
           FROM OpdsFeedEntity
         WHERE ofeUrl LIKE :urlPrefix || '%' ESCAPE '\'
           AND (:includeDeleted OR ofeStatus = ${StatusEnum.ACTIVE_INT})
          ORDER BY ofeStored DESC
     """
    )
    abstract fun findByUrlPrefixAsFlow(
        urlPrefix: String,
        includeDeleted: Boolean = false
    ): Flow<List<OpdsFeedEntity>>
}