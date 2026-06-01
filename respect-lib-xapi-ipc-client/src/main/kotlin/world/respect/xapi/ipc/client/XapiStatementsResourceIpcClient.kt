package world.respect.xapi.ipc.client

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
import kotlin.uuid.Uuid

class XapiStatementsResourceIpcClient(
    private val messenger: Messenger
): XapiStatementsResource {

    override suspend fun post(list: List<XapiStatement>): List<Uuid> {
        TODO("Not yet implemented")
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