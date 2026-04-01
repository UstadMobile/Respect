package com.ustadmobile.core.db.dao.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.daos.ActivityLangMapEntryDaoCommon
import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntry

@Dao
interface ActivityLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(entities: List<ActivityLangMapEntry>)

    /**
     * Upsert the lang map entity for an interaction entity if the related interaction entity exists
     * The interaction entity might not exist if the Activity is already defined.
     */
    @Query("""
        INSERT OR REPLACE ${ActivityLangMapEntryDaoCommon.INTO_LANG_MAP_WHERE_INTERACTION_ENTITY_EXISTS}      
    """)
    suspend fun upsertIfInteractionEntityExists(
        almeActivityUid: Long,
        almeProperty: Int,
        almeLangCode: String,
        almeValue: String,
        aieProp: Int?,
    )

    @Query("""
        UPDATE ActivityLangMapEntry
           SET almeValue = :almeValue
         WHERE almeActivityUid = :almeActivityUid
           AND almeProperty = :almeProperty
           AND almeInteractionId = :almeInteractionId
           AND almeValue != :almeValue       
    """)
    suspend fun updateIfChanged(
        almeActivityUid: Long,
        almeProperty: Int,
        almeValue: String,
        almeInteractionId: String?
    )

    @Query("""
        SELECT ActivityLangMapEntry.*
          FROM ActivityLangMapEntry
         WHERE ActivityLangMapEntry.almeActivityUid = :activityUid
    """)
    suspend fun findAllByActivityUid(activityUid: Long): List<ActivityLangMapEntry>


}