package world.respect.lib.xapi.remotewritequeue

import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.local.XapiResourceLocal

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
        TODO("Not yet implemented")
    }

}