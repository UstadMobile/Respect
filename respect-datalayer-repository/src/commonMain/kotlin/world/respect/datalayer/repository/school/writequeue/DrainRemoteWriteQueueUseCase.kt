package world.respect.datalayer.repository.school.writequeue

import io.github.aakira.napier.Napier
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.repository.SchoolDataSourceRepository
import world.respect.datalayer.school.writequeue.RemoteWriteQueue

class DrainRemoteWriteQueueUseCase(
    private val remoteWriteQueue: RemoteWriteQueue,
    private val dataSource: SchoolDataSource,
) {

    suspend operator fun invoke() {
        val pending = remoteWriteQueue.getPending(100)
        Napier.d("RemoteQueue: send ${pending.size} items")

        val remoteDataSource = (dataSource as SchoolDataSourceRepository).remote
        val localDataSource = dataSource.local
        pending.forEach { item ->
            val personsToSend = localDataSource.personDataSource.findByUidList(
                listOf(item.uid)
            )
            remoteDataSource.personDataSource.store(personsToSend)
            remoteWriteQueue.markSent(listOf(item.queueItemId))
        }
    }

}