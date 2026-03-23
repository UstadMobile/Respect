package world.respect.datalayer.school.xapi

import world.respect.datalayer.school.xapi.model.XapiStatement
import kotlin.uuid.Uuid

interface XapiStatementDataSource {

    /**
     * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     */
    data class GetStatementParams(
        val statementId: Uuid? = null,
        val voidedStatementId: Uuid? = null,
    )

    /**
     * When a statement is received through the put API, the id from the parameter must be put
     * into the statement itself.
     */
    suspend fun store(
        list: List<XapiStatement>
    )

}