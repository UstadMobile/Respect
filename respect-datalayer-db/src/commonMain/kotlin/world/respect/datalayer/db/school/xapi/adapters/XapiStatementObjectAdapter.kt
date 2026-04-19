package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.StatementEntityObjectTypeEnum
import world.respect.datalayer.school.xapi.model.XapiActivity
import world.respect.datalayer.school.xapi.model.XapiObjectType
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

val XapiObjectType.entityObjectTypeEnum: StatementEntityObjectTypeEnum
    get() = when(this) {
        XapiObjectType.StatementRef -> StatementEntityObjectTypeEnum.STATEMENT_REF
        XapiObjectType.SubStatement -> StatementEntityObjectTypeEnum.SUBSTATEMENT
        XapiObjectType.Activity -> StatementEntityObjectTypeEnum.ACTIVITY
        XapiObjectType.Agent -> StatementEntityObjectTypeEnum.AGENT
        XapiObjectType.Group -> StatementEntityObjectTypeEnum.GROUP
        else -> {
            throw IllegalArgumentException(
                "Statement cannot be an object type for a statement itself, can only be substatement"
            )
        }
    }


@OptIn(ExperimentalUuidApi::class)
fun List<XapiActivity>.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    lastModified: Instant,
) : List<XapiActivityEntities> {
    return mapNotNull { contextActivityObj ->
        contextActivityObj.toEntities(
            uidNumberMapper = uidNumberMapper,
            json = json,
            lastModified = lastModified,
        )
    }
}