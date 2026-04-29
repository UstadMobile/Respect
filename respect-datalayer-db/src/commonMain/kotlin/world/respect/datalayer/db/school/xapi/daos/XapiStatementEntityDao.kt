package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.composites.XapiSubstatementAndVerbEntity
import world.respect.datalayer.db.school.xapi.entities.XapiAssignmentProgressEntityRow
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
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

    @Query(
        """
            /*
             Begin statement query : This query is the same for both XapiStatementEntity
             based return results (used for canonical and id results) and exact (which uses 
             XapiStatementEntityJson).
             
             Normally: it would be better to handle this as a constant: however because its only in
             two places and syntax highlighting is important for it, it is copy/pasted.
            */
            
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
               JOIN XapiVerbEntity
                    ON (    XapiVerbEntity.verbUid = XapiStatementEntity.statementVerbUid)
               LEFT JOIN XapiStatementEntity AS SubStatementEntity
                    ON (    XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.SUBSTATEMENT}
                        AND SubStatementEntity.statementIdHi = XapiStatementEntity.statementObjectUid1
                        AND SubStatementEntity.statementIdLo = XapiStatementEntity.statementObjectUid2)
         WHERE (   (:statementIdHi = 0 AND :statementIdLo = 0) 
                OR (     XapiStatementEntity.statementIdHi = :statementIdHi 
                     AND XapiStatementEntity.statementIdLo = :statementIdLo))
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
           AND (:verbUid = 0 OR XapiStatementEntity.statementVerbId = :verbUid)
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
      ORDER BY CASE(:ascending) WHEN 1 THEN XapiStatementEntity.stored END ASC,
               CASE(:ascending) WHEN 0 THEN XapiStatementEntity.stored END DESC
         LIMIT :limit      
    """
    )
    suspend fun list(
        statementIdHi: Long,
        statementIdLo: Long,
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
        GROUP BY Actor.actorAccountName, Stmt.statementObjectActivityId
    """)
    fun getAssignmentProgressFlow(
        assignmentActivityUidNum: Long,
    ): Flow<List<XapiAssignmentProgressEntityRow>>


    @Query("SELECT MAX(stored) FROM XapiStatementEntity Stmt JOIN XapiStatementContextActivityJoin CtxJoin ON (Stmt.statementIdHi = CtxJoin.scajFromStatementIdHi AND Stmt.statementIdLo = CtxJoin.scajFromStatementIdLo) WHERE CtxJoin.scajToActivityUid = :activityUidNum")
    suspend fun getLastStoredTimestampForActivity(activityUidNum: Long): Long?

    companion object {

        const val SINCE_UNSET = Long.MIN_VALUE

        const val UNTIL_UNSET = Long.MAX_VALUE

    }

}
