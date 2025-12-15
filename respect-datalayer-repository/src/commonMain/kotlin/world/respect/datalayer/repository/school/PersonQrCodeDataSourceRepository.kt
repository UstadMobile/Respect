package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonQrCodeDataSourceLocal
import world.respect.datalayer.school.PersonQrDataSource
import world.respect.datalayer.school.model.PersonQrCode
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class PersonQrCodeDataSourceRepository(
    override val local: PersonQrCodeDataSourceLocal,
    override val remote: PersonQrDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
): PersonQrDataSource, RepositoryModelDataSource<PersonQrCode> {
    override suspend fun listAll(listParams: PersonQrDataSource.GetListParams): DataLoadState<List<PersonQrCode>> {
        val remote = remote.listAll(listParams)
        local.updateFromRemoteListIfNeeded(
            remote, validationHelper
        )

        return local.listAll(listParams)    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonQrCode>>> {
        return local.listAllAsFlow(loadParams, listParams).combineWithRemote(
            remoteFlow = remote.listAllAsFlow(loadParams, listParams).onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun store(list: List<PersonQrCode>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON_QRCODE,
                    uid = it.personGuid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}