package com.ustadmobile.libcache.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.libcache.db.entities.PinnedPublication

@Dao
abstract class PinnedPublicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(publication: PinnedPublication)

}