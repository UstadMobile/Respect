package world.respect.datalayer.school.xapi

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface XapiStatementDataSource {

    enum class GetStatementFormatEnum(val value: String) {
        IDS("ids"), EXACT("exact"), CANONICAL("canonical")
    }

    /**
     * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     */
    data class GetStatementParams(
        val statementId: Uuid? = null,
        val voidedStatementId: Uuid? = null,
        val agent: XapiAgent? = null,
        val verb: String? = null,
        val activity: String? = null,
        val registration: Uuid? = null,
        val relatedActivities: Boolean = false,
        val relatedAgents: Boolean = false,
        val since: Instant? = null,
        val until: Instant? = null,
        val limit: Int? = null,
        val offset: Int? = null,
        val format: GetStatementFormatEnum? = null,
        val attachments: Boolean? = null,
        val ascending: Boolean? = null,
    )

    /**
     * When a statement is received through the put API, the id from the parameter must be put
     * into the statement itself.
     */
    suspend fun store(
        list: List<XapiStatement>
    )

    suspend fun list(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<List<XapiStatement>>


}