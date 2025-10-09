package world.respect.datalayer.repository.school

import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.PersonPasswordDataSourceLocal
import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class PersonPasswordDataSourceRepository(
    override val local: PersonPasswordDataSourceLocal,
    override val remote: PersonPasswordDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
): PersonPasswordDataSource, RepositoryModelDataSource<PersonPassword> {

    override suspend fun listAll(listParams: PersonPasswordDataSource.GetListParams): DataLoadState<List<PersonPassword>> {
        val remote = remote.listAll(listParams)
        local.updateFromRemoteListIfNeeded(
            remote, validationHelper
        )

        return local.listAll(listParams)
    }

    override suspend fun store(list: List<PersonPassword>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON_PASSWORD,
                    uid = it.personGuid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}