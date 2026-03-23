package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.VerbLangMapEntry

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