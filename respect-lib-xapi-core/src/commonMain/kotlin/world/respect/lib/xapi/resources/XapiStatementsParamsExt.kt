package world.respect.lib.xapi.resources

import io.ktor.http.ParametersBuilder
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.ext.setIfNotNull
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementFormatEnum

/**
 * Set parameters for a Xapi Statements get request. All optional parameters that have default
 * values will be set to the default value for purposes of caching optimization (eg such that the
 * URL of a functionally identifiable request will be identical).
 */
fun ParametersBuilder.setXapiGetStatementsParams(
    params: XapiStatementsResource.GetStatementParams,
    json: Json,
) {
    setIfNotNull("statementId", params.statementId)
    setIfNotNull("voidedStatementId", params.voidedStatementId)
    params.agent?.also {
        set("agent", json.encodeToString(XapiActor.serializer(), it))
    }
    setIfNotNull("activity", params.activity)
    setIfNotNull("registration", params.registration)
    set("related_activities", params.relatedActivities.toString())
    set("related_agents", params.relatedAgents.toString())
    setIfNotNull("since", params.since?.toString())
    setIfNotNull("until", params.until?.toString())
    setIfNotNull("limit", params.limit?.toString())
    set("format", params.format?.value ?: GetStatementFormatEnum.EXACT.value)
    set("attachments", params.attachments.toString())
    set("ascending", params.ascending.toString())
}
