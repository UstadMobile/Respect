package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.schooldirectory.adapters.SchoolDirectoryEntryEntities
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity

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
    suspend fun findByUid(uid: Long): SchoolDirectoryEntryEntities?

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

    @Query("""
        SELECT SchoolDirectoryEntryEntity.*
          FROM SchoolDirectoryEntryEntity
         WHERE :code LIKE (SchoolDirectoryEntryEntity.reSchoolCode || '%')
    """)
    /**
     * @param code an invite code (with the directory prefix removed)
     */
    suspend fun findSchoolByInviteCode(code: String): SchoolDirectoryEntryEntity?

    @Transaction
    @Query(SELECT_LIST_SQL)
    fun listAsFlow(
        code: String?,
        name: String?,
    ): Flow<List<SchoolDirectoryEntryEntities>>

    @Transaction
    @Query(SELECT_LIST_SQL)
    suspend fun list(
        code: String?,
        name: String?,
    ): List<SchoolDirectoryEntryEntities>


    companion object {

        const val SELECT_LIST_SQL = """
        SELECT SchoolDirectoryEntryEntity.*
          FROM SchoolDirectoryEntryEntity
         WHERE (:code IS NULL OR :code LIKE (SchoolDirectoryEntryEntity.reSchoolCode || '%'))
           AND (:name IS NULL OR SchoolDirectoryEntryEntity.reUid IN
                (SELECT SchoolDirectoryEntryLangMapEntity.sdelReUid
                   FROM SchoolDirectoryEntryLangMapEntity
                  WHERE SchoolDirectoryEntryLangMapEntity.sdelValue LIKE :name))
        """

    }


}