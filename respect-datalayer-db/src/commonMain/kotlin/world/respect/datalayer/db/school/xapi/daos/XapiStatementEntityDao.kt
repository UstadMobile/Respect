package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.composites.XapiAssignmentResultRow
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.composites.XapiSubstatementAndVerbEntity
import world.respect.datalayer.db.school.xapi.composites.XapiTimes
import world.respect.datalayer.db.school.xapi.entities.XapiAssignmentProgressEntityRow
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.school.model.report.StatementReportRow

@Dao
interface XapiStatementEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<XapiStatementEntity>)

    @Query(LIST_SQL)
    suspend fun list(
        statementIdHi: Long,
        statementIdLo: Long,
        voidedStatementIdHi: Long,
        voidedStatementIdLo: Long,
        agentUid: Long,
        verbUid: Long,
        activityUid: Long,
        relatedAgents: Boolean,
        relatedActivities: Boolean,
        since: Long,
        until: Long,
        ascending: Boolean,
        limit: Int,
    ): List<XapiStatementAndJsonEntities>

    @Query(LIST_SQL)
    fun listAsFlow(
        statementIdHi: Long,
        statementIdLo: Long,
        voidedStatementIdHi: Long,
        voidedStatementIdLo: Long,
        agentUid: Long,
        verbUid: Long,
        activityUid: Long,
        relatedAgents: Boolean,
        relatedActivities: Boolean,
        since: Long,
        until: Long,
        ascending: Boolean,
        limit: Int,
    ): Flow<List<XapiStatementAndJsonEntities>>


    /**
     * When getting a StatementEntity that was used to represent a substatement, if the parent
     * statement existed, it must also exist.
     */
    @Query(
        """
        SELECT XapiStatementEntity.*, XapiVerbEntity.*
          FROM XapiStatementEntity
               JOIN XapiVerbEntity
                    ON (    XapiVerbEntity.verbUid = XapiStatementEntity.statementVerbUid)
         WHERE XapiStatementEntity.statementIdHi = :subStatementIdHi
           AND XapiStatementEntity.statementIdLo = :subStatementIdLo
           AND XapiStatementEntity.isSubStatement
    """
    )
    suspend fun getEntityForSubstatement(
        subStatementIdHi: Long,
        subStatementIdLo: Long,
    ): XapiSubstatementAndVerbEntity

    @Query("""
        SELECT XapiStatementEntity.stored AS timeStored,
               XapiStatementEntity.timestamp AS timestamp
          FROM XapiStatementEntity
         WHERE XapiStatementEntity.statementIdHi = :statementIdHi
           AND XapiStatementEntity.statementIdLo = :statementIdLo
    """)
    suspend fun getTimestampsByUuid(
        statementIdHi: Long,
        statementIdLo: Long,
    ): XapiTimes?

    @Query("""
        SELECT XapiStatementEntity.statementVerbUid
          FROM XapiStatementEntity
         WHERE XapiStatementEntity.statementIdHi = :statementIdHi
           AND XapiStatementEntity.statementIdLo = :statementIdLo
    """)
    suspend fun getVerbUidNumToBeVoided(
        statementIdHi: Long,
        statementIdLo: Long,
    ): Long?

    @Query("""
        UPDATE XapiStatementEntity
           SET stmtVoid = 1
         WHERE XapiStatementEntity.statementIdHi = :voidStmtIdHi
           AND XapiStatementEntity.statementIdLo = :voidStmtIdLo
    """)
    suspend fun updateSetStatementVoided(
        voidStmtIdHi: Long,
        voidStmtIdLo: Long
    )

    /**
     * Get a result with one row per combination of assigned actor (including group members) and
     * assigned activity id as per the assignment recipe. Each row includes summary stats for the
     * given combination: if it was completed, successful, progress, score, etc.
     *
     * This is currently based on using an aggregate query and group by to get the required results.
     * An alternative (valid) approach would be to use a CTE based on JOIN to create a row per
     * combination, and then use subqueries. The aggregate functions should be marginally more
     * efficient as it avoids the need to run a separate subquery for each field: the statements can
     * be selected, grouped, and then we can get all the required fields in one go.
     */
    @Query("""
        WITH LatestAssignmentStatementIds(idHi, idLo, actorUid) AS (
                 SELECT XapiStatementEntity.statementIdHi AS idHi,
                        XapiStatementEntity.statementIdLo AS idLo,
                        XapiStatementEntity.statementActorUid AS actorUid
                   FROM XapiStatementEntity
                  WHERE XapiStatementEntity.statementObjectUid1 = :assignmentActivityIdNum
                    AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY}
                    AND NOT XapiStatementEntity.stmtVoid
               ORDER BY XapiStatementEntity.timestamp DESC
                  LIMIT 1),
             
             AgentActorUids(uid) AS (
                   SELECT (SELECT actorUid FROM LatestAssignmentStatementIds) AS uid
                     UNION
                    SELECT XapiGroupMemberActorJoin.gmajMemberActorUid AS uid
                      FROM XapiGroupMemberActorJoin
                     WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = 
                           (SELECT actorUid FROM LatestAssignmentStatementIds)),
             
             AssignedActivityUids(assignedActivityUid) AS (
                    SELECT XapiStatementContextActivityJoin.scajToActivityUid AS assignedActivityUid
                      FROM XapiStatementContextActivityJoin
                    WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = 
                          (SELECT idHi FROM LatestAssignmentStatementIds)
                      AND XapiStatementContextActivityJoin.scajFromStatementIdLo = 
                          (SELECT idLo FROM LatestAssignmentStatementIds)  
             )

             -- Use aggregate functions with Group by actor, activity to get the required progress
             -- info per actor and activity.
             SELECT XapiStatementEntity.statementActorUid AS actorUid,
                    XapiStatementEntity.statementObjectUid1 AS activityUid,
                    MAX(XapiStatementEntity.extensionProgress) AS progress,
                    MAX(XapiStatementEntity.resultCompletion) AS completed,
                    MAX(XapiStatementEntity.resultSuccess) AS successful,
                    MAX(XapiStatementEntity.resultScoreScaled) AS scoreScaled
               FROM XapiStatementEntity
              WHERE XapiStatementEntity.statementObjectUid1 IN(
                    SELECT DISTINCT AssignedActivityUids.assignedActivityUid 
                      FROM AssignedActivityUids)
                AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY}
                AND NOT XapiStatementEntity.stmtVoid
                AND XapiStatementEntity.statementActorUid IN 
                    (SELECT uid FROM AgentActorUids)
           GROUP BY XapiStatementEntity.statementActorUid, XapiStatementEntity.statementObjectUid1
    """)
    suspend fun getAssignmentResults(
        assignmentActivityIdNum: Long
    ): List<XapiAssignmentResultRow>


    @RawQuery
    suspend fun runReportQuery(query: RoomRawQuery): List<StatementReportRow>

    @Query("""
        SELECT 
            Stmt.statementActorUid AS personUid,
            Actor.actorName AS personName,
            Stmt.statementObjectActivityId AS activityId,
            MAX(Stmt.resultCompletion) AS completion,
            MAX(Stmt.resultSuccess) AS success,
            MAX(COALESCE(Stmt.resultScoreScaled, Stmt.resultScoreRaw)) AS scoreScaled,
            MAX(Stmt.extensionProgress) AS progress
        FROM XapiStatementEntity Stmt
        JOIN XapiActorEntity Actor ON Stmt.statementActorUid = Actor.actorUid
        JOIN XapiStatementContextActivityJoin CtxJoin ON (Stmt.statementIdHi = CtxJoin.scajFromStatementIdHi AND Stmt.statementIdLo = CtxJoin.scajFromStatementIdLo)
        WHERE CtxJoin.scajToActivityUid = :assignmentActivityUidNum
        GROUP BY Stmt.statementActorUid, Stmt.statementObjectActivityId
    """)
    fun getAssignmentProgressFlow(
        assignmentActivityUidNum: Long,
    ): Flow<List<XapiAssignmentProgressEntityRow>>

    @Query("SELECT MAX(stored) FROM XapiStatementEntity Stmt JOIN XapiStatementContextActivityJoin CtxJoin ON (Stmt.statementIdHi = CtxJoin.scajFromStatementIdHi AND Stmt.statementIdLo = CtxJoin.scajFromStatementIdLo) WHERE CtxJoin.scajToActivityUid = :activityUidNum")
    suspend fun getLastStoredTimestampForActivity(activityUidNum: Long): Long?

    companion object {

        const val SINCE_UNSET = Long.MIN_VALUE

        const val UNTIL_UNSET = Long.MAX_VALUE

        /*
         Begin statement query : This query is the same for both XapiStatementEntity
         based return results (used for canonical and id results) and exact (which uses
         XapiStatementEntityJson).

         Normally: it would be better to handle this as a constant: however because its only in
         two places and syntax highlighting is important for it, it is copy/pasted.
        */
        // language=RoomSql
        const val LIST_SQL = """
            
            -- Get a list of all the actors that should be considered as related to agent parameter
            -- Eg the actor uid itself and any group members
          WITH AgentActorUids(uid) AS
               (SELECT :agentUid AS uid
                 UNION
                SELECT XapiGroupMemberActorJoin.gmajGroupActorUid AS uid
                  FROM XapiGroupMemberActorJoin
                 WHERE :agentUid != 0 
                   AND XapiGroupMemberActorJoin.gmajMemberActorUid = :agentUid)
            
            
        SELECT XapiStatementEntity.*, XapiVerbEntity.*
          FROM XapiStatementEntity
               LEFT JOIN XapiVerbEntity
                    ON (    XapiVerbEntity.verbUid = XapiStatementEntity.statementVerbUid)
               LEFT JOIN XapiStatementEntity AS SubStatementEntity
                    ON (    XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.SUBSTATEMENT}
                        AND SubStatementEntity.statementIdHi = XapiStatementEntity.statementObjectUid1
                        AND SubStatementEntity.statementIdLo = XapiStatementEntity.statementObjectUid2)
         WHERE (   (:statementIdHi = 0 AND :statementIdLo = 0) 
                OR (     XapiStatementEntity.statementIdHi = :statementIdHi 
                     AND XapiStatementEntity.statementIdLo = :statementIdLo))
           AND (   (:voidedStatementIdHi = 0 AND :voidedStatementIdLo = 0)
                OR (     XapiStatementEntity.statementIdHi = :voidedStatementIdHi 
                     AND XapiStatementEntity.statementIdLo = :voidedStatementIdLo
                     AND XapiStatementEntity.stmtVoid))
           AND (:since = $SINCE_UNSET OR XapiStatementEntity.stored > :since)
           AND (:until = $UNTIL_UNSET OR XapiStatementEntity.stored <= :until)
           -- Handle agent parameter
           AND (    :agentUid = 0 
                  OR (    XapiStatementEntity.statementActorUid IN 
                          (SELECT uid FROM AgentActorUids))
                  OR (    (    XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.AGENT}
                            OR XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.GROUP})
                      AND (XapiStatementEntity.statementObjectUid1 IN (SELECT uid FROM AgentActorUids)))
                  -- Handle related agents        
                  OR (    :relatedAgents 
                      AND (    XapiStatementEntity.authorityActorUid IN (SELECT uid FROM AgentActorUids)
                            OR XapiStatementEntity.contextTeamActorUid IN (SELECT uid FROM AgentActorUids)
                            OR XapiStatementEntity.contextInstructorActorUid IN (SELECT uid FROM AgentActorUids)
                            -- check substatement if applicable
                            OR (     SubStatementEntity.statementActorUid IS NOT NULL
                                AND (    SubStatementEntity.statementActorUid IN (SELECT uid FROM AgentActorUids)
                                      OR (     (   SubStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.AGENT}
                                                OR SubStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.GROUP})
                                           AND (SubStatementEntity.statementObjectUid1 IN (SELECT uid FROM AgentActorUids)))
                                           
                                      OR (SubStatementEntity.authorityActorUid IN (SELECT uid FROM AgentActorUids))
                                      OR (SubStatementEntity.contextTeamActorUid IN (SELECT uid FROM AgentActorUids))
                                      OR (SubStatementEntity.contextInstructorActorUid IN (SELECT uid FROM AgentActorUids))  
                                    )
                               )       
                          )
                     )
               )
           AND (:verbUid = 0 OR XapiStatementEntity.statementVerbUid = :verbUid)
               -- Handle activity parameter
           AND (      :activityUid = 0
                   OR (     XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY}
                        AND XapiStatementEntity.statementObjectUid1 = :activityUid)
                   OR (     :relatedActivities    
                        AND (    -- As per spec check if substatement activity matches when relatedActivities is set  
                                 (     SubStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY} 
                                   AND SubStatementEntity.statementObjectUid1 = :activityUid)
                                 -- As per spec check if activity uid is part included in context activities.   
                               OR (:activityUid IN 
                                   (SELECT XapiStatementContextActivityJoin.scajToActivityUid
                                      FROM XapiStatementContextActivityJoin
                                     WHERE (    XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                                            AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo)
                                        OR (    SubStatementEntity.statementIdHi IS NOT NULL 
                                            AND XapiStatementContextActivityJoin.scajFromStatementIdHi = SubStatementEntity.statementIdHi
                                            AND XapiStatementContextActivityJoin.scajFromStatementIdLo = SubStatementEntity.statementIdLo)     
                                   )
                                  )    
                            )
                      ) 
               )          
           AND NOT XapiStatementEntity.isSubStatement
           AND (    NOT XapiStatementEntity.stmtVoid
                 OR (:voidedStatementIdHi != 0 AND :voidedStatementIdLo != 0))
      ORDER BY CASE(:ascending) WHEN 1 THEN XapiStatementEntity.stored END ASC,
               CASE(:ascending) WHEN 0 THEN XapiStatementEntity.stored END DESC
         LIMIT :limit      

        """

    }

}