package com.ustadmobile.core.db.dao.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityLangMapEntry

@Dao
interface XapiActivityLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(entities: List<XapiActivityLangMapEntry>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(entities: List<XapiActivityLangMapEntry>)

    @Query(
        """
        UPDATE XapiActivityLangMapEntry
           SET almeValue = :almeValue
         WHERE almeActivityUid = :almeActivityUid
           AND almeProperty = :almeProperty
           AND almeInteractionId = :almeInteractionId
           AND almeLastModified > :changeTime
           AND almeValue != :almeValue
    """
    )
    suspend fun updateIfChanged(
        almeActivityUid: Long,
        almeProperty: Int,
        almeValue: String,
        almeInteractionId: String?,
        changeTime: Long,
    )

    @Query(
        """
        SELECT XapiActivityLangMapEntry.*
          FROM XapiActivityLangMapEntry
         WHERE XapiActivityLangMapEntry.almeActivityUid = :activityUid
    """
    )
    suspend fun findAllByActivityUid(activityUid: Long): List<XapiActivityLangMapEntry>


}