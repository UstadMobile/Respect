package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Query
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity

@Dao
interface SchoolDirectoryEntityDao {
    @Query(
        """
            SELECT * FROM SchoolDirectoryEntity
        """
    )
    suspend fun getSchoolDirectories():List<SchoolDirectoryEntity>
}