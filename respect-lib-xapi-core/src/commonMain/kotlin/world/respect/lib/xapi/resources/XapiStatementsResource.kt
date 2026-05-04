package world.respect.lib.xapi.resources

import io.ktor.util.StringValues
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.lib.xapi.XapiResponseHeaders
import world.respect.lib.xapi.ext.getUuidOrNull
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import kotlin.uuid.Uuid

/**
 * The xAPI Statements Resource
 */
interface XapiStatementsResource {

    enum class GetStatementFormatEnum(val value: String) {
        IDS("ids"), EXACT("exact"), CANONICAL("canonical");

        companion object {

            fun fromValue(value: String): GetStatementFormatEnum {
                return entries.first { it.value == value }
            }
        }
    }

    /**
     * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     */
    @Serializable
    data class GetStatementParams(
        val statementId: Uuid? = null,
        val voidedStatementId: Uuid? = null,
        val agent: XapiAgent? = null,
        val verb: String? = null,
        val activity: String? = null,
        val registration: Uuid? = null,
        val relatedActivities: Boolean = false,
        val relatedAgents: Boolean = false,
        val since: InstantAsISO8601? = null,
        val until: InstantAsISO8601? = null,
        val limit: Int? = null,
        val format: GetStatementFormatEnum? = null,
        val attachments: Boolean = false,
        val ascending: Boolean = false,
    ) {

        companion object {

            fun fromParams(
                params: StringValues,
                json: Json,
            ): GetStatementParams {
                return GetStatementParams(
                    statementId = params.getUuidOrNull("statementId"),
                    voidedStatementId = params.getUuidOrNull("voidedStatementId"),
                    agent = params["agent"]?.let { json.decodeFromString(it) },
                    verb = params["verb"],
                    activity = params["activity"],
                    registration = params.getUuidOrNull("registration"),
                    relatedActivities = params["related_activities"]?.toBoolean() ?: false,
                    relatedAgents = params["related_agents"]?.toBoolean() ?: false,
                    limit = params["limit"]?.toInt(),
                    format = params["format"]?.let { GetStatementFormatEnum.fromValue(it) },
                    attachments = params["attachments"]?.toBoolean() ?: false,
                    ascending = params["ascending"]?.toBoolean() ?: false,
                )

            }
        }
    }

    data class GetStatementsRequest(
        val params: GetStatementParams,
        val headers: XapiRequestHeaders,
    )

    data class GetStatementsResponse(
        val statementResult: XapiStatementResult,
        val headers: XapiResponseHeaders,
    )

    /**
     * When a statement is received through the put API, the id from the parameter must be put
     * into the statement itself.
     */
    suspend fun post(
        list: List<XapiStatement>
    ): List<Uuid>

    suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<XapiStatementResult>

    companion object {

        const val ENDPOINT_NAME = "statements"

    }
}