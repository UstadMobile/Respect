package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.resources.XapiStatementsResource

fun XapiStatementsResource.GetStatementParams.toBundle(
    json: Json,
): Bundle {
    return Bundle().also {
        it.putUuidIfNotNull("statementId", statementId)
        it.putUuidIfNotNull("voidedStatementId", voidedStatementId)
        it.putStringIfNotNull(
            key = "agent",
            value = agent?.let { actor ->
                json.encodeToString(XapiActor.serializer(), actor)
            }
        )
        it.putStringIfNotNull("verb", verb)
        it.putStringIfNotNull("activity", activity)
        it.putUuidIfNotNull("registration", registration)
        it.putBoolean("related_activities", relatedActivities)
        it.putBoolean("related_agents", relatedAgents)
        it.putLongIfNotNull("since", since?.toEpochMilliseconds())
        it.putLongIfNotNull("until", until?.toEpochMilliseconds())
        it.putIntIfNotNull("limit", limit)
        it.putStringIfNotNull("format", format?.value)
        it.putBoolean("attachments", attachments)
        it.putBoolean("ascending", ascending)
    }
}


