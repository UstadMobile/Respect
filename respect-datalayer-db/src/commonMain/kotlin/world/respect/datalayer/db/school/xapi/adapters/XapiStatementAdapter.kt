package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.ext.toLongPair
import world.respect.datalayer.db.school.xapi.entities.StatementEntity
import world.respect.datalayer.db.school.xapi.entities.StatementEntityJson
import world.respect.datalayer.db.school.xapi.entities.StatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
import world.respect.datalayer.school.xapi.ext.isCompletionOrProgress
import world.respect.datalayer.school.xapi.ext.resultProgressExtension
import world.respect.datalayer.school.xapi.model.XapiActivityStatementObject
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiException
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiStatementObject
import world.respect.datalayer.school.xapi.model.XapiStatementRef
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.ext.toEmptyIfNull
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * @property statements a list because even a single statement can require multiple StatementEntity(s)
 *           when it includes a sub statement.
 */
data class StatementEntities(
    val statements: List<StatementEntity> = emptyList(),
    val statementEntityJson: List<StatementEntityJson> = emptyList(),
    val actorEntities: List<ActorEntities> = emptyList(),
    val verbEntities: List<VerbEntities> = emptyList(),
    val activityEntities: List<ActivityEntities> = emptyList(),
)

fun List<StatementEntities>.flatten(): StatementEntities {
    return StatementEntities(
        statements = flatMap { it.statements },
        statementEntityJson = flatMap { it.statementEntityJson },
        actorEntities = flatMap { it.actorEntities },
        verbEntities = flatMap { it.verbEntities },
        activityEntities = flatMap { it.activityEntities }
    )
}


/**
 * As per the spec, if the objectType is not specified, it defaults to Activity.
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2441-when-the-objecttype-is-activity
 */
val XapiStatementObject.objectTypeEnum: StatementEntityObjectTypeEnum
    get() = objectType?.entityObjectTypeEnum ?: StatementEntityObjectTypeEnum.ACTIVITY

@OptIn(ExperimentalUuidApi::class)
fun XapiStatementObject.objectForeignKeys(
    uidNumberMapper: UidNumberMapper,
    statementUuid: Uuid,
): Pair<Long, Long> {
    return when(this) {
        is XapiActivityStatementObject -> {
            Pair(uidNumberMapper(id), 0)
        }
        is XapiAgent -> {
            Pair(this.identifierHash(uidNumberMapper), 0)
        }
        is XapiGroup -> {
            Pair(this.identifierHash(uidNumberMapper), 0)
        }
        is XapiStatementRef -> {
            Uuid.parse(id).toLongs { mostSignificantBits, leastSignificantBits ->
                Pair(mostSignificantBits, leastSignificantBits)
            }
        }
        is XapiStatement -> {
            //As per the doc on StatementEntity itself, where there is a substatement, the
            //statement uid is the uid of the statement itself + 1.
            statementUuid.toLongs { mostSignificantBits, leastSignificantBits ->
                Pair(mostSignificantBits, leastSignificantBits + 1)
            }
        }
    }
}

/**
 * Convert xAPI Statement JSON instead entities that can be stored in the database.
 *
 * Most of the time the statement received will be when running an xAPI activity, and the actor will
 * be an Agent for the current user.
 *
 * @param isSubStatement true if the statement being processed is a substatement, in which case, it
 * must not as per the spec contain another nested substatement.
 *
 * @return list of two statement entities - the statement itself, and the entities of the object (
 * which could be a substatement, agent, group, or statementref)
 */
@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.toEntities(
    uidNumberMapper: UidNumberMapper,
    primaryKeyGenerator: PrimaryKeyGenerator,
    json: Json,
    exactJson: String?,
    isSubStatement: Boolean = false,
): StatementEntities {
    val statementUuid = id ?: throw IllegalArgumentException("Statement must have id set before conversion to entities")

    if(isSubStatement && `object` is XapiStatement)
        throw XapiException(
            400,
            "SubStatement cannot have another nested subs== XapiObjectType.SubStatementtatement"
        )

    val contextRegistration = context?.registration

    val statementActorEntities = actor.toEntities(
        uidNumberMapper, primaryKeyGenerator
    )

    val authorityActor = authority?.toEntities(
        uidNumberMapper, primaryKeyGenerator,
    )

    val contextInstructorActorEntities = context?.instructor?.toEntities(
        uidNumberMapper, primaryKeyGenerator,
    )

    val statementObjectForeignKeys = `object`.objectForeignKeys(uidNumberMapper, statementUuid)

    val (stmtUuidHi, stmtUuidLo) = statementUuid.toLongPair()
    val contextRegHiLo = contextRegistration?.toLongPair()

    return listOf(
        StatementEntities(
            statements = listOf(
                StatementEntity(
                    statementIdHi = stmtUuidHi,
                    statementIdLo = stmtUuidLo,
                    statementActorUid = statementActorEntities.actor.actorUid,
                    authorityActorUid = authorityActor?.actor?.actorUid ?: 0,
                    statementVerbUid = uidNumberMapper(verb.id),
                    resultCompletion = result?.completion,
                    resultSuccess = result?.success,
                    resultScoreScaled = result?.score?.scaled,
                    resultScoreRaw = result?.score?.raw,
                    resultScoreMin = result?.score?.min,
                    resultScoreMax = result?.score?.max,
                    resultDuration = result?.duration?.inWholeMilliseconds,
                    resultResponse = result?.response,
                    timestamp = timestamp?.toEpochMilliseconds() ?: systemTimeInMillis(),
                    stored = systemTimeInMillis(),
                    contextRegistrationHi = contextRegHiLo?.first ?: 0,
                    contextRegistrationLo = contextRegHiLo?.second ?: 0,
                    contextPlatform = context?.platform,
                    contextInstructorActorUid = contextInstructorActorEntities?.actor?.actorUid ?: 0,
                    completionOrProgress = isCompletionOrProgress(),
                    extensionProgress = resultProgressExtension,
                    statementObjectType = `object`.objectTypeEnum,
                    statementObjectUid1 = statementObjectForeignKeys.first,
                    statementObjectUid2 = statementObjectForeignKeys.second,
                    isSubStatement = isSubStatement,
                )
            ),
            statementEntityJson = listOf(
                StatementEntityJson(
                    stmtJsonIdHi = stmtUuidHi,
                    stmtJsonIdLo = stmtUuidLo,
                    fullStatement = exactJson,
                )
            ),
            actorEntities = buildList {
                add(statementActorEntities)
                contextInstructorActorEntities?.also { add(it) }
            },
            verbEntities = listOf(verb.toVerbEntities(uidNumberMapper)),
            /*
             * Note: object.objectToEntities will generate the ActivityEntities where an the object
             * of the statement is an activity.
             */
            activityEntities = context?.contextActivities
                ?.toEntities(
                    uidNumberMapper = uidNumberMapper,
                    json = json,
                    statementUuid = statementUuid,
                ).toEmptyIfNull()
        ),
        `object`.objectToEntities(
            uidNumberMapper = uidNumberMapper,
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            parentStatementUuid = statementUuid,
        )
    ).flatten()
}


fun StatementEntities.toModel() : XapiStatement {
    val primaryStatementEntity = statements.first { !it.isSubStatement }
    val actors = actorEntities.associate { it.actor.actorUid to it.toModel() }

    return XapiStatement(
        id = Uuid.fromLongs(
            primaryStatementEntity.statementIdHi,
            primaryStatementEntity.statementIdLo
        ),
        actor = actors[primaryStatementEntity.statementActorUid] ?: throw IllegalStateException("no primary actor"),
        verb = this.verbEntities.first {
            primaryStatementEntity.statementVerbUid == it.verbEntity.verbUid
        }.toModel(),
        `object` = TODO()//when(primaryStatementEntity.statementObjectType)
    )
}

