package world.respect.datasource.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datasource.db.entities.LangMapEntity

@Dao
abstract class LangMapEntityDao {

    @Insert
    abstract suspend fun insertAsync(entity: List<LangMapEntity>)

    @Query("""
        DELETE FROM LangMapEntity
         WHERE LangMapEntity.lmeTableId = :lmeTableId
           AND LangMapEntity.lmeEntityUid1 = :lmeEntityUid1
           AND LangMapEntity.lmeEntityUid2 = :lmeEntityUid2
    """)
    abstract suspend fun deleteByTableAndEntityUid(
        lmeTableId: Int,
        lmeEntityUid1: Long,
        lmeEntityUid2: Long = 0,
    )

    @Query("""
        SELECT * 
          FROM LangMapEntity
         WHERE LangMapEntity.lmeTableId = :lmeTableId
    """)
    abstract fun selectAllByTableId(
        lmeTableId: Int,
    ): Flow<List<LangMapEntity>>

}