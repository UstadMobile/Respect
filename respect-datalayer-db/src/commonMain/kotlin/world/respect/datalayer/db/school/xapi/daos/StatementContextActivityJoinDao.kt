package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoin

@Dao
interface StatementContextActivityJoinDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<StatementContextActivityJoin>)

    @Query(
        """
        SELECT StatementContextActivityJoin.*
          FROM StatementContextActivityJoin
         WHERE (      StatementContextActivityJoin.scajFromStatementIdHi = :statementIdHi
                  AND StatementContextActivityJoin.scajFromStatementIdLo = :statementIdLo)
            OR (     (:statementIdHi2 != 0 AND :statementIdLo2 != 0) 
                 AND StatementContextActivityJoin.scajFromStatementIdHi = :statementIdHi2
                 AND StatementContextActivityJoin.scajFromStatementIdLo = :statementIdLo2)
    """)
    suspend fun findAllByStatementIds(
        statementIdHi: Long,
        statementIdLo: Long,
        statementIdHi2: Long,
        statementIdLo2: Long,
    ): List<StatementContextActivityJoin>
}