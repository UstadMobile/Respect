package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.VerbLangMapEntry

@Dao
interface VerbLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(list: List<VerbLangMapEntry>)

    @Query(
        """
        SELECT VerbLangMapEntry.*
          FROM VerbLangMapEntry
         WHERE VerbLangMapEntry.vlmeVerbUid = :verbUid
    """
    )
    suspend fun findByVerbUidAsync(verbUid: Long): List<VerbLangMapEntry>
}