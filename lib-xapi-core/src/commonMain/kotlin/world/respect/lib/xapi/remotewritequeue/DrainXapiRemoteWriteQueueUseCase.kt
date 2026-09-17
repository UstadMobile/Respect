package world.respect.lib.xapi.remotewritequeue

import io.ktor.http.parseQueryString
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.local.XapiResourceLocal
import kotlin.uuid.Uuid

/**
 *
 */
class DrainXapiRemoteWriteQueueUseCase(
    private val xapiRemoteWriteQueue: XapiRemoteWriteQueue,
    private val remoteDataSource: XapiResource,
    private val localDataSource: XapiResourceLocal,
) {

    /**
     * Drain the Xapi Remote Write queue.
     */
    suspend operator fun invoke() {
        val pendingItems = xapiRemoteWriteQueue.getPending(DEFAULT_BATCH_SIZE)

        for(queueItem in pendingItems) {
            when(queueItem.resource) {
                XapiRemoteWriteQueueItem.Resource.STATEMENTS -> {
                    val statement = localDataSource.statements.get(
                        listParams = XapiStatementsResource.GetStatementParams(
                            statementId = Uuid.parse(queueItem.itemId),
                            format = XapiStatementsResource.GetStatementFormatEnum.EXACT,
                        )
                    ).dataOrNull()?.statements?.firstOrNull() ?: continue

                    remoteDataSource.statements.post(listOf(statement))
                    xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                }

                XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE -> {
                    val params = XapiActivityProfileResource.SingleDocumentParams.fromParams(
                        parseQueryString(queueItem.itemId)
                    )
                    val document = localDataSource.activityProfile.get(params).dataOrNull() ?: continue

                    when(queueItem.method) {
                        XapiRemoteWriteQueueItem.Method.POST -> {
                            remoteDataSource.activityProfile.post(params, document)
                        }

                        XapiRemoteWriteQueueItem.Method.PUT -> {
                            remoteDataSource.activityProfile.post(params, document)
                        }

                        XapiRemoteWriteQueueItem.Method.DELETE -> {
                            //not implemented yet
                        }
                    }

                    xapiRemoteWriteQueue.markSent(listOf(queueItem.xrqItemId))
                }
            }

        }
    }

    companion object {

        const val DEFAULT_BATCH_SIZE = 10
    }

}