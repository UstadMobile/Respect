package world.respect.datalayer.repository.school

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.combineWithRemoteIfNotNull
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonQrCodeDataSourceLocal
import world.respect.datalayer.school.PersonQrDataSource
import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.DataLayerTags
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class PersonQrCodeDataSourceRepository(
    override val local: PersonQrCodeDataSourceLocal,
    override val remote: PersonQrDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : PersonQrDataSource, RepositoryModelDataSource<PersonBadge> {

    override suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: PersonQrDataSource.GetListParams
    ): DataLoadState<List<PersonBadge>> {
        val remote = try {
            remote.listAll(loadParams, listParams).also {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        } catch (e: Throwable) {
            Napier.w(
                message = "PersonQrCodeDataSourceRepository.list() failed:",
                throwable = e,
                tag = DataLayerTags.TAG_DATALAYER
            )
            null
        }

        return local.listAll(loadParams, listParams).combineWithRemoteIfNotNull(remote)
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonBadge>>> {
        return local.listAllAsFlow(loadParams, listParams).combineWithRemote(
            remoteFlow = remote.listAllAsFlow(loadParams, listParams.copy(includeDeleted = true))
                .onEach {
                    local.updateFromRemoteListIfNeeded(it, validationHelper)
                }
        )
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonBadge>> {
        return local.findByGuidAsFlow(loadParams, guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(loadParams, guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun existsByQrCodeUrl(url: String, uidNum: Long): Boolean {
        return local.existsByQrCodeUrl(url, uidNum)
    }

    override suspend fun store(list: List<PersonBadge>) {
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