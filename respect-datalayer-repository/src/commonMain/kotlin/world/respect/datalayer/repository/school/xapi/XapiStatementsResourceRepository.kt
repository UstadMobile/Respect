package world.respect.datalayer.repository.school.xapi

import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.school.xapi.XapiStatementsResourceLocal
import world.respect.datalayer.school.xapi.ext.idStr
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.model.XapiVerb
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
            val remoteResult = remote.get(
                listParams = listParams.copy(
                    format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
                ),
                dataLoadParams = dataLoadParams,
            )
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
            remote.getAsFlow(
                listParams = listParams.copy(
                    format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
                ),
                dataLoadParams = dataLoadParams,
            ).onEach { remoteState ->
                val remoteData = remoteState.dataOrNull()
                if(remoteData != null) {
                    local.updateLocal(remoteData.statements)
                }
            }
        )
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?,
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        return local.getAssignmentProgress(
            activityId = activityId,
            filterByAssigneeAgent = filterByAssigneeAgent,
        ).combineWithRemote(
            remoteFlow = remote.getAsFlow(
                listParams = GetStatementParams(
                    activity = activityId,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams()
            ).onEach { remoteState ->
                val remoteData = remoteState.dataOrNull()
                if(remoteData != null) {
                    local.updateLocal(remoteData.statements)
                }
            }
        )
    }

    override fun getAssignmentListAsFlow(
        dataLoadParams: DataLoadParams,
        studentAgent: XapiAgent?
    ): Flow<DataLoadState<List<AssignmentSummary>>> {
        return channelFlow {
            val actorsToLoadFlow = MutableSharedFlow<List<XapiActor>>(
                replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
            )

            launch {
                local.getAssignmentListAsFlow(
                    dataLoadParams, studentAgent
                ).collectLatest { loadState ->
                    send(loadState)

                    val actors = loadState.dataOrNull()?.map { it.assignedActor }
                        ?.distinctBy { it.idStr }

                    if(!actors.isNullOrEmpty()) {
                        actorsToLoadFlow.emit(actors)
                    }
                }
            }

            launch {
                actorsToLoadFlow.distinctUntilChanged().collectLatest { actors ->
                    actors.forEach { actor ->
                        launch {
                            this@XapiStatementsResourceRepository.get(
                                listParams = GetStatementParams(
                                    agent = actor,
                                    verb = XapiVerb.ID_SAVED,
                                )
                            )
                        }
                    }
                }
            }

            remote.get(
                listParams = GetStatementParams(
                    verb = XapiVerb.ID_ASSIGN,
                )
            ).also {
                val remoteData = it.dataOrNull()
                if(remoteData != null) {
                    local.updateLocal(remoteData.statements)
                }
            }

            awaitClose {
                Napier.d("Closing assignment list flow")
            }
        }

    }
}