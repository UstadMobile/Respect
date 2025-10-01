package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.ktor.http.Url
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity
import world.respect.datalayer.respect.model.RespectSchoolDirectory

@Dao
interface SchoolDirectoryEntityDao {
    @Query(
        """
            SELECT * FROM SchoolDirectoryEntity
        """
    )
    suspend fun getSchoolDirectories(): List<SchoolDirectoryEntity>

    @Query(
        """
        SELECT SchoolDirectoryEntity.*
          FROM SchoolDirectoryEntity
         WHERE :code LIKE (SchoolDirectoryEntity.rdInvitePrefix || '%')
    """
    )
    suspend fun getSchoolDirectoryByInviteCode(
        code: String
    ): SchoolDirectoryEntity?

    @Query(
        """
        SELECT SchoolDirectoryEntity.*
          FROM SchoolDirectoryEntity
         WHERE SchoolDirectoryEntity.rdUrl = '${RespectSchoolDirectory.SERVER_MANAGED_DIRECTORY_URL}'
    """
    )
    suspend fun getServerManagerSchoolDirectory(): SchoolDirectoryEntity?

    @Query(
        """
        DELETE FROM SchoolDirectoryEntity WHERE rdUrl = :url
        """
    )
    suspend fun deleteByUrl(url: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SchoolDirectoryEntity)

}