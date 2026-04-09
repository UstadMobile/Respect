package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.StatementEntityObjectTypeEnum
import world.respect.datalayer.db.school.xapi.ext.uuidForSubstatement
import world.respect.datalayer.school.xapi.model.XapiActivityStatementObject
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiStatementObject
import world.respect.datalayer.school.xapi.model.XapiStatementRef
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
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

/**
 * Convert the statement object into entities. Because the object could be an activity, substatement,
 * statementref, agent, or group, this function returns StatementEntities itself
 */
@OptIn(ExperimentalUuidApi::class)
fun XapiStatementObject.objectToEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    parentStatementUuid: Uuid,
) : StatementEntities {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    return when(this) {
        is XapiActivityStatementObject -> {
            StatementEntities(
                activityEntities = listOf(
                    definition.toEntities(id, uidNumberMapper, json)
                )
            )
        }

        is XapiActor -> {
            StatementEntities(
                actorEntities = listOf(toEntities(uidNumberMapper))
            )
        }

        is XapiStatementRef -> {
            //When the object is a statement ref, there are no other entities. Its just a link by uuid
            StatementEntities()
        }

        is XapiStatement -> {
            this.copy(
                id = parentStatementUuid.uuidForSubstatement(),
            ).toEntities(
                uidNumberMapper = uidNumberMapper,
                json = json,
                exactJson = null,
                isSubStatement = true,
            )
        }

        else -> {
            throw IllegalStateException("This cant really happen. The compiler does not recognize " +
                    "XapiActor as covering XapiGroup and XapiAgent, but it does.")
        }
    }
}


@OptIn(ExperimentalUuidApi::class)
fun List<XapiActivityStatementObject>.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
) : List<ActivityEntities> {
    return mapNotNull { contextActivityObj ->
        contextActivityObj.definition?.toEntities(
            activityId = contextActivityObj.id,
            uidNumberMapper = uidNumberMapper,
            json = json,
        )
    }
}