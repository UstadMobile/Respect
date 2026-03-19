package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.SchoolConfigSettingEntity

@Dao
interface SchoolConfigSettingEntityDao {

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE ((:key IS NULL) OR scsKey = :key)
           AND ((:since = 0) OR (scsStored > :since))
    """)
    fun listAsFlow(
        key: String? = null,
        since: Long = 0,
    ): Flow<List<SchoolConfigSettingEntity>>

}