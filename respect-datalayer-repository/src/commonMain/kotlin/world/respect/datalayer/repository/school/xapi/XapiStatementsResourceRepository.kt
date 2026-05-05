package world.respect.datalayer.repository.school.xapi

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.school.xapi.XapiStatementsResourceLocal
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.AssignmentResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import kotlin.time.Clock
import kotlin.uuid.Uuid

class XapiStatementsResourceRepository(
    private val local: XapiStatementsResourceLocal,
    private val remote: XapiStatementsResource,
    private val remoteWriteQueue: RemoteWriteQueue,
) : XapiStatementsResource{

    override suspend fun post(
        list: List<XapiStatement>
    ): List<Uuid> {
        val uuidsSaved = local.post(list)

        val timeNow = Clock.System.now().toEpochMilliseconds()

        remoteWriteQueue.add(
            uuidsSaved.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.XAPI_STATEMENT,
                    uid = it.toString(),
                    timeQueued = timeNow,
                )
            }
        )

        return uuidsSaved
    }

    override suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiStatementResult> {
        try {
            val remoteResult = remote.get(listParams, dataLoadParams)
            remoteResult.dataOrNull()?.statements.takeIf { it?.isNotEmpty() == true }?.also {
                local.updateLocal(it)
            }
        }catch(e: Throwable) {
            Napier.w("Could not contact remote", e)
        }

        return local.get(listParams, dataLoadParams)
    }

    override fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        return local.getAsFlow(
            listParams = listParams, dataLoadParams = dataLoadParams
        ).combineWithRemote(
            remote.getAsFlow(listParams, dataLoadParams).onEach { remoteState ->
                val remoteData = remoteState.dataOrNull()
                if(remoteData != null) {
                    local.updateLocal(remoteData.statements)
                }
            }
        )
    }


    override fun getAssignmentResult(assignmentActivityId: String): Flow<List<AssignmentResult>> {
        val remoteSyncFlow = getAsFlow(
            listParams = GetStatementParams(activity = assignmentActivityId),
            dataLoadParams = DataLoadParams()
        )

        return local.getAssignmentResult(assignmentActivityId).combine(remoteSyncFlow) { localData, _ ->
            localData
        }
    }

    override suspend fun getLastStoredTimestampForActivity(activityId: String): Long? {
        return local.getLastStoredTimestampForActivity(activityId)
    }
}
