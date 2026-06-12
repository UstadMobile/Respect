package world.respect.xapi.ipc.client

import android.os.Message
import android.os.Messenger
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
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
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import kotlin.uuid.Uuid

class XapiStatementsResourceIpcClient(
    private val requestSender: MessageRequestSender,
    private val json: Json,
    private val endpoint: Url,
    private val auth: String,

    ): XapiStatementsResource {

    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        val message = Message.obtain(
            null, XapiIpcWhatFlags.WHAT_REQUEST, 0, XapiIpcResourceFlags.POST_STATEMENTS
        )

        message.data.putString(XapiIpcKeys.KEY_ENDPOINT, endpoint.toString())
        message.data.putString(XapiIpcKeys.KEY_AUTH, auth)

        message.data.putString(
            XapiIpcKeys.KEY_BODY,
            json.encodeToString(ListSerializer(XapiStatement.serializer()), list)
        )

        val response = requestSender.sendRequest(message)
        val uuidsCreated = response.data.getString(XapiIpcKeys.KEY_BODY)?.let {
            json.decodeFromString(ListSerializer(Uuid.serializer()), it)
        } ?: throw IllegalStateException("IPC Response has no body")

        return uuidsCreated
    }

    override suspend fun get(
        listParams: XapiStatementsResource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiStatementResult> {
        //messenger.send()




        TODO("Not yet implemented")
    }

    override fun getAsFlow(
        listParams: XapiStatementsResource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        TODO("Not yet implemented")
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        TODO("Not yet implemented")
    }

    override fun getAssignmentListAsFlow(
        dataLoadParams: DataLoadParams,
        studentAgent: XapiAgent?
    ): Flow<DataLoadState<List<AssignmentSummary>>> {
        TODO("Not yet implemented")
    }
}