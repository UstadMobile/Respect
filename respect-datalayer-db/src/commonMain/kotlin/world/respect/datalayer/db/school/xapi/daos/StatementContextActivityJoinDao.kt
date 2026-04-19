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
         WHERE StatementContextActivityJoin.scajFromStatementIdHi = :statementIdHi
           AND StatementContextActivityJoin.scajFromStatementIdLo = :statementIdLo
    """
    )
    suspend fun findAllByStatementId(
        statementIdHi: Long,
        statementIdLo: Long,
    ): List<StatementContextActivityJoin>
}