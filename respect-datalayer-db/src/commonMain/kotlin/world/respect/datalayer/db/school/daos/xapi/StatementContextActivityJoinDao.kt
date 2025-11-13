package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.StatementContextActivityJoin

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
           AND StatementContextActivityJoin.scajContextType = :scajContextType
    """
    )
    suspend fun findAllByStatementId(
        statementIdHi: Long,
        statementIdLo: Long,
        scajContextType: Int,
    ): List<StatementContextActivityJoin>
}