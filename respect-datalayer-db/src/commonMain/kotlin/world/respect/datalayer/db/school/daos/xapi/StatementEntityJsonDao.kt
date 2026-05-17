package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.StatementEntityJson

@Dao
interface StatementEntityJsonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<StatementEntityJson>)

    @Query(
        """
        SELECT StatementEntityJson.*
          FROM StatementEntityJson
         WHERE (    (:stmtJsonIdHi = 0 AND :stmtJsonIdLo = 0) 
                 OR (stmtJsonIdHi = :stmtJsonIdHi AND stmtJsonIdLo = :stmtJsonIdLo))
                  
    """
    )
    suspend fun getStatements(
        stmtJsonIdHi: Long,
        stmtJsonIdLo: Long,
    ): List<StatementEntityJson>
}