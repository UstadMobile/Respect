package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.school.model.report.StatementReportRow

@Dao
interface XapiStatementEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<XapiStatementEntity>)

    @Query("SELECT * From XapiStatementEntity LIMIT 1")
    fun getOneStatement(): Flow<XapiStatementEntity?>

    @Query("SELECT * FROM XapiStatementEntity")
    suspend fun getAll(): List<XapiStatementEntity>

    @Query("""
        SELECT XapiStatementEntity.*, XapiStatementEntityJson.*
          FROM XapiStatementEntity
               JOIN XapiStatementEntityJson
                    ON (    XapiStatementEntityJson.stmtJsonIdHi = XapiStatementEntity.statementIdHi
                        AND XapiStatementEntityJson.stmtJsonIdLo = XapiStatementEntity.statementIdLo)
         WHERE (   (:statementIdHi = 0 AND :statementIdLo = 0) 
                OR (     XapiStatementEntity.statementIdHi = :statementIdHi 
                     AND XapiStatementEntity.statementIdLo = :statementIdLo))
           AND NOT XapiStatementEntity.isSubStatement           
    """)
    suspend fun list(
        statementIdHi: Long,
        statementIdLo: Long,
    ): List<XapiStatementAndJsonEntities>

    /**
     * When getting a StatementEntity that was used to represent a substatement, if the parent
     * statement existed, it must also exist.
     */
    @Query("""
        SELECT XapiStatementEntity.*
          FROM XapiStatementEntity
         WHERE XapiStatementEntity.statementIdHi = :subStatementIdHi
           AND XapiStatementEntity.statementIdLo = :subStatementIdLo
           AND XapiStatementEntity.isSubStatement
    """)
    suspend fun getEntityForSubstatement(
        subStatementIdHi: Long,
        subStatementIdLo: Long,
    ): XapiStatementEntity


    @RawQuery
    suspend fun runReportQuery(query: RoomRawQuery): List<StatementReportRow>

}