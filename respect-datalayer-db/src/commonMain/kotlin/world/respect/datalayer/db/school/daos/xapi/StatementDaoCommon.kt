package world.respect.datalayer.db.school.daos.xapi

import world.respect.datalayer.db.school.entities.xapi.XapiEntityObjectTypeFlags
import world.respect.datalayer.db.shared.daos.SystemPermissionDaoCommon
import world.respect.datalayer.db.shared.ext.PermissionFlags

object StatementDaoCommon {

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT = """
          FROM StatementEntity
         WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
           AND StatementEntity.statementContentEntryUid = :contentEntryUid
           AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
           AND (:courseBlockUid = 0 OR StatementEntity.statementCbUid = :courseBlockUid)
    """

    const val FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY = """
            $FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT
        AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
              OR (StatementEntity.extensionProgress IS NOT NULL))
        
        
    """

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT = """
        FROM StatementEntity
       WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
         AND StatementEntity.statementContentEntryUid IN (
             SELECT ContentEntryParentChildJoin.cepcjChildContentEntryUid
               FROM ContentEntryParentChildJoin
              WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid)
         AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
         AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
              OR (StatementEntity.extensionProgress IS NOT NULL))     
    """

    const val STATEMENT_ENTITY_IS_SUCCESSFUL_COMPLETION_CLAUSE = """
              CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1    
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1
    """

    const val STATEMENT_ENTITY_IS_FAILED_COMPLETION_CLAUSE = """
              CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
          AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0
    """


    //If the GroupMemberActorJoin does not match the statement and person, it will be null
    // This should be optimized: use a CTE to find the actor uids for persons that in the query
    const val STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS = """
            StatementEntity.statementCbUid = PersonUidsAndCourseBlocks.cbUid
        AND StatementEntity.statementActorUid IN (
            SELECT ActorUidsForPersonUid.actorUid
              FROM ActorUidsForPersonUid
             WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid)  
                   
    """

    //Same as above, modified for where tables are using an _inner postfix
    const val STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS_INNER = """
            StatementEntity_Inner.statementCbUid = PersonUidsAndCourseBlocks.cbUid
        AND StatementEntity_Inner.statementActorUid IN (
            SELECT ActorUidsForPersonUid.actorUid
              FROM ActorUidsForPersonUid
             WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid)  
                   
    """

    //Join the actor entity, and, if relevant, the GroupMemberActorJoin for the actor group and
    //person as per ActorUidsForPersonUid
    const val JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID = """
       JOIN ActorEntity
            ON ActorEntity.actorUid = StatementEntity.statementActorUid
       LEFT JOIN GroupMemberActorJoin
            ON ActorEntity.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
               AND (GroupMemberActorJoin.gmajGroupActorUid, GroupMemberActorJoin.gmajMemberActorUid) IN (
                   SELECT GroupMemberActorJoin.gmajGroupActorUid, 
                          GroupMemberActorJoin.gmajMemberActorUid
                     FROM GroupMemberActorJoin
                    WHERE GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                      AND GroupMemberActorJoin.gmajMemberActorUid IN (
                          SELECT ActorUidsForPersonUid.actorUid
                            FROM ActorUidsForPersonUid
                           WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid))
    """

    const val JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID_INNER = """
       JOIN ActorEntity ActorEntity_Inner
            ON ActorEntity_Inner.actorUid = StatementEntity_Inner.statementActorUid
       LEFT JOIN GroupMemberActorJoin GroupMemberActorJoin_Inner
            ON ActorEntity_Inner.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
               AND (GroupMemberActorJoin_Inner.gmajGroupActorUid, GroupMemberActorJoin_Inner.gmajMemberActorUid) IN (
                   SELECT GroupMemberActorJoin.gmajGroupActorUid, 
                          GroupMemberActorJoin.gmajMemberActorUid
                     FROM GroupMemberActorJoin
                    WHERE GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                      AND GroupMemberActorJoin.gmajMemberActorUid IN (
                          SELECT ActorUidsForPersonUid.actorUid
                            FROM ActorUidsForPersonUid
                           WHERE ActorUidsForPersonUid.actorPersonUid = PersonUidsAndCourseBlocks.personUid))
    """


    const val ACTOR_UIDS_FOR_PERSONUIDS_CTE = """
        -- Get the ActorUids for the PersonUids See ActoryEntity doc for info on this join relationship
        AgentActorUidsForPersonUid(actorUid, actorPersonUid) AS(
             SELECT ActorEntity.actorUid AS actorUid, 
                    ActorEntity.actorPersonUid AS actorPersonUid
               FROM ActorEntity
              WHERE ActorEntity.actorPersonUid IN
                    (SELECT PersonUids.personUid
                       FROM PersonUids)           
        ),
        
        -- Add in group actor uids
        ActorUidsForPersonUid(actorUid, actorPersonUid) AS (
             SELECT AgentActorUidsForPersonUid.actorUid AS actorUid,
                    AgentActorUidsForPersonUid.actorPersonUid AS actorPersonUid
               FROM AgentActorUidsForPersonUid     
              UNION 
             SELECT GroupMemberActorJoin.gmajGroupActorUid AS actorUid,
                    AgentActorUidsForPersonUid.actorPersonUid AS actorPersonUid
               FROM AgentActorUidsForPersonUid
                    JOIN GroupMemberActorJoin 
                         ON GroupMemberActorJoin.gmajMemberActorUid = AgentActorUidsForPersonUid.actorUid
        )
    """


    const val SELECT_STATUS_STATEMENTS_FOR_ACTOR_PERSON_UIDS = """
        -- Fetch all statements that could be completion or progress for the Gradebook report
        SELECT StatementEntity.*, ActorEntity.*, GroupMemberActorJoin.*
          FROM StatementEntity
               JOIN ActorEntity
                    ON ActorEntity.actorUid = StatementEntity.statementActorUid
               LEFT JOIN GroupMemberActorJoin
                    ON ActorEntity.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
                       AND GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                       AND GroupMemberActorJoin.gmajMemberActorUid IN (
                           SELECT DISTINCT ActorUidsForPersonUid.actorUid
                             FROM ActorUidsForPersonUid)
         WHERE StatementEntity.statementClazzUid = :clazzUid
           AND StatementEntity.completionOrProgress = :completionOrProgressTrueVal
           AND StatementEntity.statementActorUid IN (
               SELECT DISTINCT ActorUidsForPersonUid.actorUid
                 FROM ActorUidsForPersonUid) 
           AND (      StatementEntity.resultScoreScaled IS NOT NULL
                   OR StatementEntity.resultCompletion IS NOT NULL
                   OR StatementEntity.resultSuccess IS NOT NULL
                   OR StatementEntity.extensionProgress IS NOT NULL 
               )
    """

    const val PERSON_WITH_ATTEMPTS_MAXSCORE = """
             SELECT MAX(StatementEntity.resultScoreScaled)
               FROM StatementEntity
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                AND StatementEntity.statementActorPersonUid = Person.personUid
                AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
    """

    const val PERSON_WITH_ATTEMPTS_MAX_PROGRESS = """
             SELECT MAX(StatementEntity.extensionProgress)
               FROM StatementEntity
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                AND StatementEntity.statementActorPersonUid = Person.personUid
                AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
    """

    const val PERSON_WITH_ATTEMPTS_MOST_RECENT_TIME = """
        SELECT MAX(StatementEntity.timestamp)
               FROM StatementEntity
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                AND StatementEntity.statementActorPersonUid = Person.personUid
    """


    const val FROM_STATEMENTS_WHERE_MATCHES_CONTENT_ENTRY_UID_AND_HAS_PERMISSION = """
        FROM StatementEntity
                    LEFT JOIN ClazzEnrolment 
                         ON ClazzEnrolment.clazzEnrolmentUid =
                           COALESCE(
                            (SELECT ClazzEnrolment.clazzEnrolmentUid 
                               FROM ClazzEnrolment
                              WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                                AND ClazzEnrolment.clazzEnrolmentActive
                                AND ClazzEnrolment.clazzEnrolmentClazzUid = StatementEntity.statementClazzUid 
                           ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                              LIMIT 1), 0)
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                /* permission check */
                AND (    StatementEntity.statementActorPersonUid = :accountPersonUid
                      OR EXISTS(SELECT CoursePermission.cpUid
                                  FROM CoursePermission
                                 WHERE CoursePermission.cpClazzUid = StatementEntity.statementClazzUid
                                   AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                                        OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                                   AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0 
                                   AND NOT CoursePermission.cpIsDeleted)
                      OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
                          ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                          ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2}))
    """

    const val DISTINCT_REGISTRATION_UIDS_FOR_PERSON_AND_CONTENT = """
             DistinctRegistrationUids(contextRegistrationHi, contextRegistrationLo, statementClazzUid) AS (
      SELECT DISTINCT StatementEntity.contextRegistrationHi, 
                     StatementEntity.contextRegistrationLo,
                     StatementEntity.statementClazzUid
                 FROM StatementEntity
                WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                  AND StatementEntity.statementActorPersonUid = :personUid)
    """

    const val DISTINCT_REGISTRATION_UIDS_PERMISSION_CHECK = """
            :personUid = :accountPersonUid 
                OR EXISTS(
                    SELECT CoursePermission.cpUid
                      FROM CoursePermission
                           LEFT JOIN ClazzEnrolment 
                                ON ClazzEnrolment.clazzEnrolmentUid =
                                  COALESCE(
                                   (SELECT ClazzEnrolment.clazzEnrolmentUid 
                                      FROM ClazzEnrolment
                                     WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                                       AND ClazzEnrolment.clazzEnrolmentActive
                                       AND ClazzEnrolment.clazzEnrolmentClazzUid = DistinctRegistrationUids.statementClazzUid 
                                  ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                                     LIMIT 1), 0)
                     WHERE CoursePermission.cpClazzUid = DistinctRegistrationUids.statementClazzUid
                       AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                            OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                       AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0 
                       AND NOT CoursePermission.cpIsDeleted)
                OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
                    ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                    ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2})
    """


}