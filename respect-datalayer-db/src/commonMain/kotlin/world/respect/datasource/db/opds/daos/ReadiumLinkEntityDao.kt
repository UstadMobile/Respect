package world.respect.datasource.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datasource.db.opds.OpdsParentType
import world.respect.datasource.db.opds.daos.OpdsPublicationEntityDao.Companion.PUBLICATION_UIDS_FOR_FEED_UID_CTE
import world.respect.datasource.db.opds.entities.ReadiumLinkEntity

@Dao
abstract class ReadiumLinkEntityDao {

    @Query("""
         WITH $PUBLICATION_UIDS_FOR_FEED_UID_CTE 

       SELECT ReadiumLinkEntity.*
         FROM ReadiumLinkEntity
        WHERE $LINK_ENTITIES_FOR_FEEDUID_WHERE_CLAUSE
    """)
    abstract suspend fun findAllByFeedUid(feedUid: Long): List<ReadiumLinkEntity>

    @Query("""
        SELECT ReadiumLinkEntity.*
          FROM ReadiumLinkEntity
         WHERE ReadiumLinkEntity.rleOpdsParentType = ${OpdsParentType.ID_PUBLICATION}
           AND ReadiumLinkEntity.rleOpdsParentUid = :publicationUid
    """)
    abstract suspend fun findAllByPubUid(publicationUid: Long): List<ReadiumLinkEntity>

    @Query("""
        WITH $PUBLICATION_UIDS_FOR_FEED_UID_CTE 
        
       DELETE
         FROM ReadiumLinkEntity
        WHERE $LINK_ENTITIES_FOR_FEEDUID_WHERE_CLAUSE
    """)
    abstract suspend fun deleteAllByFeedUid(feedUid: Long)

    @Query(""" 
       DELETE
         FROM ReadiumLinkEntity
        WHERE ReadiumLinkEntity.rleOpdsParentUid = :publicationUid
          AND ReadiumLinkEntity.rleOpdsParentType = ${OpdsParentType.ID_PUBLICATION}
    """)
    abstract suspend fun deleteAllByPublicationUid(publicationUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<ReadiumLinkEntity>)

    companion object {
        const val LINK_ENTITIES_FOR_FEEDUID_WHERE_CLAUSE = """
              (     ReadiumLinkEntity.rleOpdsParentType = ${OpdsParentType.ID_FEED}
                AND ReadiumLinkEntity.rleOpdsParentUid = :feedUid)
          OR  (     ReadiumLinkEntity.rleOpdsParentType = ${OpdsParentType.ID_PUBLICATION}
                AND ReadiumLinkEntity.rleOpdsParentUid IN (SELECT publicationUid FROM FeedPublicationUids))  
        """
    }
}