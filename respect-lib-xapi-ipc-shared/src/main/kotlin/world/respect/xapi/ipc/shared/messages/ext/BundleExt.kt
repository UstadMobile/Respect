package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementFormatEnum
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun Bundle.putUuid(key: String, uuid: Uuid) {
    putString(key, uuid.toString())
}

fun Bundle.putUuidIfNotNull(key: String, uuid: Uuid?) {
    uuid?.also { putUuid(key, it) }
}

fun Bundle.getUuidOrNull(key: String): Uuid? {
    return getString(key, null)?.let { Uuid.parse(it) }
}

fun Bundle.putStringIfNotNull(key: String, value: String?) {
    value?.also { putString(key, value) }
}

fun Bundle.putLongIfNotNull(key: String, value: Long?) {
    value?.also { putLong(key ,value) }
}

fun Bundle.getLongOrNull(key: String): Long? {
    return if(containsKey(key))
        getLong(key)
    else
        null
}

fun Bundle.putIntIfNotNull(key: String, value: Int?) {
    value?.also { putInt(key ,value) }
}

fun Bundle.getIntOrNull(key: String): Int? {
    return if(containsKey(key))
        getInt(key)
    else
        null
}

fun Bundle.toGetStatementParams(
    json: Json
): XapiStatementsResource.GetStatementParams {
    return XapiStatementsResource.GetStatementParams(
        statementId = getUuidOrNull("statementId"),
        voidedStatementId = getUuidOrNull("voidedStatementId"),
        agent = getString("agent")?.let {
            json.decodeFromString(XapiActor.serializer(), it)
        },
        verb = getString("verb"),
        activity = getString("activity"),
        registration = getUuidOrNull("registration"),
        relatedActivities = getBoolean("related_activities"),
        relatedAgents = getBoolean("related_agents"),
        since = getLongOrNull("since")?.let {
            Instant.fromEpochMilliseconds(it)
        },
        until = getLongOrNull("until")?.let {
            Instant.fromEpochMilliseconds(it)
        },
        limit = getIntOrNull("limit"),
        format = getString("format")?.let { GetStatementFormatEnum.valueOf(it) },
        attachments = getBoolean("attachments"),
        ascending = getBoolean("ascending"),
    )
}

