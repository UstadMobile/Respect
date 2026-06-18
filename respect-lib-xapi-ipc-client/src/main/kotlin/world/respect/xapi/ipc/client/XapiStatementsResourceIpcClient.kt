package world.respect.xapi.ipc.client

import android.os.Bundle
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.shared.messages.MessageData
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import world.respect.xapi.ipc.shared.messages.ext.putStringValues
import world.respect.xapi.ipc.shared.messages.ext.toDataLoadState
import kotlin.uuid.Uuid

class XapiStatementsResourceIpcClient(
    private val requestSender: XapiMessageBridge,
    private val json: Json,
    private val endpoint: Url,
    private val auth: String,
): XapiStatementsResource {

    private fun Bundle.putEndpoint() {
        putString(XapiIpcKeys.KEY_ENDPOINT, endpoint.toString())
        putString(XapiIpcKeys.KEY_AUTH, auth)
    }

    override suspend fun post(list: List<XapiStatement>): DataLoadState<List<Uuid>> {
        return requestSender.executeRequestAsDataLoadState(
            request = MessageData(
                data = Bundle().apply {
                    putEndpoint()
                    putString(
                        XapiIpcKeys.KEY_BODY,
                        json.encodeToString(ListSerializer(XapiStatement.serializer()), list)
                    )
                },
                what = XapiIpcWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.POST_STATEMENTS,
            ),
            json = json,
            deserializer = ListSerializer(Uuid.serializer())
        )
    }

    override suspend fun get(
        listParams: XapiStatementsResource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiStatementResult> {
        return requestSender.executeRequestAsDataLoadState(
            request = MessageData(
                data = Bundle().apply {
                    putEndpoint()
                    putStringValues(
                        key = XapiIpcKeys.KEY_QUERY_PARAMS,
                        value = listParams.toParameters(json)
                    )
                },
                what = XapiIpcWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.GET_STATEMENTS,
            ),
            json = json,
            deserializer = XapiStatementResult.serializer()
        )
    }

    override fun getAsFlow(
        listParams: XapiStatementsResource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        return requestSender.executeForFlow(
            messageData = MessageData(
                data = Bundle().apply {
                    putEndpoint()
                    putStringValues(
                        key = XapiIpcKeys.KEY_QUERY_PARAMS,
                        value = listParams.toParameters(json)
                    )
                },
                what = XapiIpcWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.GET_STATEMENTS_FLOW,
            )
        ).map { msg ->
            msg.data.toDataLoadState(json, XapiStatementResult.serializer())
        }
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        throw IllegalStateException("GetAssignmentResults over IPC is not supported")
    }

    override fun getAssignmentListAsFlow(
        dataLoadParams: DataLoadParams,
        studentAgent: XapiAgent?
    ): Flow<DataLoadState<List<AssignmentSummary>>> {
        throw IllegalStateException("GetAssignmentResults over IPC is not supported")
    }
}