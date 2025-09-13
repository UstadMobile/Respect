package world.respect.datalayer.school.writequeue

/**
 * Represents a queue of items to be sent to the remote datasource. This queue is used by the
 * Repository. When write operations are run on the repository it will update the local datasource
 * and then add a WriteQueueItem for the items changed.
 *
 * The write queue is drained by a job (e.g. running through WorkManager on Android) that will send
 * write items to the network datasource when a connection is available and resolves any conflicts.
 */
interface RemoteWriteQueue {

    suspend fun add(items: List<WriteQueueItem>)

    suspend fun getPending(limit: Int): List<WriteQueueItem>

    suspend fun markSent(ids: List<Int>)

}