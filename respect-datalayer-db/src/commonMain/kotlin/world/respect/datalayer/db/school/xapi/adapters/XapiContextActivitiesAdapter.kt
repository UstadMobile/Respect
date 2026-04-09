package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.ext.toLongPair
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoin
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoinTypeEnum
import world.respect.datalayer.school.xapi.model.XapiActivityStatementObject
import world.respect.datalayer.school.xapi.model.XapiContextActivities
import world.respect.libutil.ext.toEmptyIfNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class ActivityEntitiesAndStatementContextActivityJoins(
    val activityEntities: List<ActivityEntities>,
    val statementContextActivityJoins: List<StatementContextActivityJoin>,
)

@OptIn(ExperimentalUuidApi::class)
fun XapiContextActivities.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    statementUuid: Uuid,
) : ActivityEntitiesAndStatementContextActivityJoins {
    fun List<XapiActivityStatementObject>?.toActivityEntitiesInternal(
        type: StatementContextActivityJoinTypeEnum,
    ) : List<ActivityEntities> {
        return this?.toEntities(uidNumberMapper, json).toEmptyIfNull()
    }

    val statementUuidLongs = statementUuid.toLongPair()

    fun XapiActivityStatementObject.toContextActivityJoinEntity(
        type: StatementContextActivityJoinTypeEnum,
    ) : StatementContextActivityJoin {
        return StatementContextActivityJoin(
            scajFromStatementIdHi = statementUuidLongs.first,
            scajFromStatementIdLo = statementUuidLongs.second,
            scajContextType = type,
            scajToActivityId = this.id,
            scajToActivityUid = uidNumberMapper(this.id)
        )
    }

    return ActivityEntitiesAndStatementContextActivityJoins(
        activityEntities = parent.toActivityEntitiesInternal(StatementContextActivityJoinTypeEnum.PARENT) +
                grouping.toActivityEntitiesInternal(StatementContextActivityJoinTypeEnum.GROUPING) +
                category.toActivityEntitiesInternal(StatementContextActivityJoinTypeEnum.CATEGORY) +
                other.toActivityEntitiesInternal(StatementContextActivityJoinTypeEnum.OTHER),
        statementContextActivityJoins = buildList {
            parent?.map {
                it.toContextActivityJoinEntity(StatementContextActivityJoinTypeEnum.PARENT)
            }?.also { addAll(it) }

            grouping?.map {
                it.toContextActivityJoinEntity(StatementContextActivityJoinTypeEnum.GROUPING)
            }?.also { addAll(it) }

            category?.map {
                it.toContextActivityJoinEntity(StatementContextActivityJoinTypeEnum.CATEGORY)
            }?.also { addAll(it) }

            other?.map {
                it.toContextActivityJoinEntity(StatementContextActivityJoinTypeEnum.OTHER)
            }?.also { addAll(it) }
        },
    )
}
