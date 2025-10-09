package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.PersonPasswordEntity

@Dao
interface PersonPasswordEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(personPasswordEntity: PersonPasswordEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsyncList(list: List<PersonPasswordEntity>)

    @Query(
        """
           SELECT PersonPasswordEntity.* 
             FROM PersonPasswordEntity
            WHERE ppwGuidNum = :uidNum
        """
    )
    suspend fun findByUid(uidNum: Long): PersonPasswordEntity?

    @Query("""
        SELECT PersonPasswordEntity.ppwLastModified
          FROM PersonPasswordEntity
         WHERE PersonPasswordEntity.ppwGuidNum = :uidNum
    """)
    suspend fun getLastModifiedByPersonUidNum(
        uidNum: Long
    ): Long?

    @Query("""
        SELECT PersonPasswordEntity.*
          FROM PersonPasswordEntity
         WHERE PersonPasswordEntity.ppwGuidNum IN (:uids)
    """)
    suspend fun findByUidList(
        uids: List<Long>
    ) : List<PersonPasswordEntity>


    @Query("""
        SELECT PersonPasswordEntity.*
          FROM PersonPasswordEntity
         WHERE PersonPasswordEntity.ppwGuidNum = :personGuidNum
    """)
    suspend fun findAll(
        personGuidNum: Long
    ): List<PersonPasswordEntity>

}