package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoin
import world.respect.datalayer.school.xapi.model.XapiActivityStatementObject
import world.respect.datalayer.school.xapi.model.XapiContextActivities
import world.respect.libutil.ext.toEmptyIfNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid



@OptIn(ExperimentalUuidApi::class)
fun XapiContextActivities.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    statementUuid: Uuid,
) : List<ActivityEntities> {
    fun List<XapiActivityStatementObject>?.toEntitiesInternal(type: Int) : List<ActivityEntities> {
        return this?.toEntities(uidNumberMapper, json, statementUuid, type).toEmptyIfNull()
    }

    return parent.toEntitiesInternal(StatementContextActivityJoin.TYPE_PARENT) +
            grouping.toEntitiesInternal(StatementContextActivityJoin.TYPE_GROUPING) +
            category.toEntitiesInternal(StatementContextActivityJoin.TYPE_CATEGORY) +
            other.toEntitiesInternal(StatementContextActivityJoin.TYPE_OTHER)
}
