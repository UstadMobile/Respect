package world.respect.datalayer.repository.school.writequeue

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.repository.SchoolDataSourceRepository
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource


class DrainRemoteWriteQueueUseCase(
    private val remoteWriteQueue: RemoteWriteQueue,
    private val dataSource: SchoolDataSource,
) {

    private suspend fun <T: Any> RepositoryModelDataSource<T>.sendToRemote(
        writeQueueItems: List<WriteQueueItem>
    ) {
        val data = local.findByUidList(writeQueueItems.map { it.uid })
        remote.store(data)
        remoteWriteQueue.markSent(writeQueueItems.map { it.queueItemId })
    }

    suspend operator fun invoke() {
        val repository = dataSource as SchoolDataSourceRepository
        do {
            val pending = remoteWriteQueue.getPending(100)
            if(pending.isEmpty())
                break

            pending.forEach { item ->
                when(item.model) {
                    WriteQueueItem.Model.PERSON -> {
                        repository.personDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.CLASS -> {
                        repository.classDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.ENROLLMENT -> {
                        repository.enrollmentDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.PERSON_PASSWORD -> {
                        repository.personPasswordDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.ASSIGNMENT -> {
                        repository.assignmentDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.SCHOOL_APP -> {
                        repository.schoolAppDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.SCHOOL_PERMISSION_GRANT -> {
                        repository.schoolPermissionGrantDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.INVITE -> {
                        repository.inviteDataSource.sendToRemote(listOf(item))
                    }
                }

            }
        } while(true)
    }

}