package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.db.entities.DownloadJobItem
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DownloadJobItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertList(items: List<DownloadJobItem>)

    @Query("""
        SELECT DownloadJobItem.*
          FROM DownloadJobItem
         WHERE DownloadJobItem.djiDjUid = :jobUid
           AND DownloadJobItem.djiStatus < ${TransferJobItemStatus.STATUS_COMPLETE_INT}
    """)
    abstract suspend fun findPendingByJobUid(jobUid: Int): List<DownloadJobItem>

    @Query("""
        SELECT DownloadJobItem.*
          FROM DownloadJobItem
         WHERE DownloadJobItem.djiDjUid = :jobUid
    """)
    abstract suspend fun findAllByJobUid(jobUid: Int): List<DownloadJobItem>

    @Query("""
       SELECT COUNT(*)
         FROM DownloadJobItem
        WHERE DownloadJobItem.djiDjUid = :jobUid
          AND DownloadJobItem.djiStatus != ${TransferJobItemStatus.STATUS_COMPLETE_INT}
    """)
    abstract suspend fun findNumberJobItemsNotComplete(
        jobUid: Int,
    ): Int

    @Query("""
        UPDATE DownloadJobItem
           SET djiTransferred = :transferred
         WHERE djiUid = :jobItemUid
    """)
    abstract suspend fun updateTransferredProgress(
        jobItemUid: Int,
        transferred: Long,
    )


    @Query("""
        UPDATE DownloadJobItem
           SET djiStatus = :status
         WHERE djiUid = :jobItemUid  
    """)
    abstract suspend fun updateStatus(
        jobItemUid: Int,
        status: Int,
    )

    @Query("""
        WITH MostRecentDlUid(uid) AS (
            SELECT COALESCE(
                   (SELECT DownloadJob.djUid
                      FROM DownloadJob
                     WHERE DownloadJob.djPubManifestHash = :pubManifestHash
                  ORDER BY DownloadJob.djTimeCreated DESC
                     LIMIT 1), 0)
        )
        
        SELECT CASE((SELECT DownloadJob.djStatus
                       FROM DownloadJob
                      WHERE DownloadJob.djUid = (SELECT uid FROM MostRecentDlUid)))
                   WHEN ${TransferJobItemStatus.STATUS_COMPLETE_INT} THEN ${PublicationPinState.Status.READY_STATUS_INT}
                   WHEN ${TransferJobItemStatus.STATUS_IN_PROGRESS_INT} THEN ${PublicationPinState.Status.IN_PROGRESS_INT}
                   WHEN ${TransferJobItemStatus.STATUS_PREPARING_INT} THEN ${PublicationPinState.Status.PREPARING_INT}
                   ELSE ${PublicationPinState.Status.NOT_PINNED_INT}
                   END AS status,
               (SELECT SUM(DownloadJobItem.djiTransferred)
                  FROM DownloadJobItem
                 WHERE DownloadJobItem.djiDjUid = (SELECT uid FROM MostRecentDlUid)) AS transferred,
               (SELECT SUM(DownloadJobItem.djiTotalSize)
                  FROM DownloadJobItem
                 WHERE DownloadJobItem.djiDjUid = (SELECT uid FROM MostRecentDlUid)) AS totalSize  
    """)
    abstract fun publicationPinState(
        pubManifestHash: Long,
    ): Flow<PublicationPinState>



}