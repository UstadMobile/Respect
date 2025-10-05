package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity
import world.respect.datalayer.respect.model.RespectSchoolDirectory

@Dao
interface SchoolDirectoryEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(schoolDirectory: SchoolDirectoryEntity)

    @Query(
        """
            SELECT * FROM SchoolDirectoryEntity
        """
    )
    suspend fun getSchoolDirectories():List<SchoolDirectoryEntity>

    @Query("""
        SELECT SchoolDirectoryEntity.*
          FROM SchoolDirectoryEntity
         WHERE SchoolDirectoryEntity.rdUrl = '${RespectSchoolDirectory.SERVER_MANAGED_DIRECTORY_URL}'
    """)
    suspend fun getServerManagerSchoolDirectory(): SchoolDirectoryEntity?

    @Query("""
        DELETE FROM SchoolDirectoryEntity
         WHERE rdUid != :exceptUid
    """)
    suspend fun deleteOthers(
        exceptUid: Long
    )

}