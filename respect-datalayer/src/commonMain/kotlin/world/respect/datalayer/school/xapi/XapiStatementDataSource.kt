package world.respect.datalayer.school.xapi

import io.ktor.util.StringValues
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.respect.datalayer.school.ClassDataSource
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.lib.xapi.XapiResponseHeaders
import world.respect.lib.xapi.ext.getUuidOrNull
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import kotlin.uuid.Uuid

interface XapiStatementDataSource {

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
        val attachments: Boolean? = null,
        val ascending: Boolean? = null,
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
                    attachments = params["attachments"]?.toBoolean(),
                    ascending = params["ascending"]?.toBoolean()
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
        request: GetStatementsRequest,
    ): GetStatementsResponse

}