package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityJson

@Dao
interface XapiStatementEntityJsonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<XapiStatementEntityJson>)

    @Query(
        """
        SELECT XapiStatementEntityJson.*
          FROM XapiStatementEntityJson
         WHERE (    (:stmtJsonIdHi = 0 AND :stmtJsonIdLo = 0) 
                 OR (stmtJsonIdHi = :stmtJsonIdHi AND stmtJsonIdLo = :stmtJsonIdLo))
                  
    """
    )
    suspend fun getStatements(
        stmtJsonIdHi: Long,
        stmtJsonIdLo: Long,
    ): List<XapiStatementEntityJson>
}