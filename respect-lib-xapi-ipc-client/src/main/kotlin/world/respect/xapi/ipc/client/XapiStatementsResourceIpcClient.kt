package world.respect.xapi.ipc.client

import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import kotlin.uuid.Uuid

class XapiStatementsResourceIpcClient(
    private val requestSender: MessageRequestSender
): XapiStatementsResource {

    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        val message = Message.obtain(
            null, XapiIpcWhatFlags.WHAT_REQUEST, 0, XapiIpcResourceFlags.POST_STATEMENTS
        )
        val response = requestSender.sendRequest(message)
        return emptyList()
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