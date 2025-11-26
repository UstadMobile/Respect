package world.respect.datalayer.repository.school

import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.InviteDataSourceLocal
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource

class InviteDataSourceRepository(
    override val local: InviteDataSourceLocal,
    override val remote: InviteDataSource,
    private val remoteWriteQueue: RemoteWriteQueue
) : InviteDataSource, RepositoryModelDataSource<Invite> {


    override suspend fun store(list: List<Invite>) {
        local.store(list)
        val timeNow = System.currentTimeMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.INVITE,
                    uid = it.guid,
                    timeQueued = timeNow
                )
            }
        )
    }
}
