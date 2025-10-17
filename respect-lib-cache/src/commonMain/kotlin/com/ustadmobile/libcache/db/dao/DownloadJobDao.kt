package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.libcache.db.entities.DownloadJob
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus

@Dao
abstract class DownloadJobDao {

    @Insert
    abstract suspend fun insert(downloadJob: DownloadJob): Long

    @Query(
        """
        SELECT * 
          FROM DownloadJob 
         WHERE djUid = :uid
    """
    )
    abstract suspend fun findByUid(uid: Int): DownloadJob?

    @Query("""
        UPDATE DownloadJob
           SET djStatus = ${TransferJobItemStatus.STATUS_COMPLETE_INT}
         WHERE djUid = :jobUid
          AND NOT EXISTS(
              SELECT DownloadJobItem.djiUid
                FROM DownloadJobItem
               WHERE DownloadJobItem.djiDjUid = :jobUid
                 AND DownloadJobItem.djiStatus != ${TransferJobItemStatus.STATUS_COMPLETE_INT}) 
    """)
    abstract suspend fun updateStatusSetCompleteIfAllItemsComplete(jobUid: Int): Int


    @Query("""
        UPDATE DownloadJob
           SET djStatus = :status
         WHERE djUid = :jobUid
    """)
    abstract suspend fun updateStatus(
        jobUid: Int,
        status: Int
    )

}