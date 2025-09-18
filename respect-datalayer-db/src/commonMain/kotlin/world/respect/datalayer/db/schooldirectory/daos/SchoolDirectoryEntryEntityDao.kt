package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity
import world.respect.datalayer.respect.model.SchoolDirectoryEntry

@Dao
interface SchoolDirectoryEntryEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(realmEntity: SchoolDirectoryEntryEntity)

    @Query(
        """
        SELECT * 
         FROM SchoolDirectoryEntryEntity
        WHERE reUid = :uid
    """
    )
    suspend fun findByUid(uid: Long): SchoolDirectoryEntryEntity?


    @Query(
        """
         SELECT *
           FROM SchoolDirectoryEntryEntity
          WHERE reSchoolCode LIKE :code || '%'
    """
    )
    suspend fun findSchoolByInviteCode(code: String): SchoolDirectoryEntryEntity?

    @Query(
        """
        SELECT SchoolDirectoryEntryEntity.*
          FROM SchoolDirectoryEntryEntity
               JOIN LangMapEntity
                    ON LangMapEntity.lmeTopParentUid1 = SchoolDirectoryEntryEntity.reUid
         WHERE LangMapEntity.lmeValue LIKE :query
     """
    )
    fun searchSchoolsByName(query: String): Flow<List<SchoolDirectoryEntryEntity>>


}