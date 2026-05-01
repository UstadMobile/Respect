package world.respect.datalayer.repository.school.xapi

import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.school.xapi.XapiStatementsResourceLocal
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
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
        request: XapiStatementsResource.GetStatementsRequest
    ): XapiStatementsResource.GetStatementsResponse {
        TODO("Not yet implemented")
    }
}