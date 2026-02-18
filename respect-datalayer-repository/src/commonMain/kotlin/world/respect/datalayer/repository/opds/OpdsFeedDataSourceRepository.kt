package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.datalayer.school.opds.ext.requireSelfUrl
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.lib.opds.model.OpdsFeed
import world.respect.libutil.util.time.systemTimeInMillis

class OpdsFeedDataSourceRepository(
    override val local: OpdsFeedDataSourceLocal,
    override val remote: OpdsFeedDataSource,
    private val remoteWriteQueue: RemoteWriteQueue,
): OpdsFeedDataSource, RepositoryModelDataSource<OpdsFeed> {

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return local.getByUrlAsFlow(
            url = url,
            params = params
        ).combineWithRemote(
            remoteFlow = remote.getByUrlAsFlow(
                url = url,
                params = params
            ).onEach { remoteData ->
                local.updateFromRemoteIfNeeded(
                    remoteLoad = remoteData,
                    validationHelper = null,
                )
            }
        )
    }

    override suspend fun store(list: List<OpdsFeed>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map { feed  ->
                WriteQueueItem(
                    model = WriteQueueItem.Model.OPDS_FEED,
                    uid = feed.requireSelfUrl().toString(),
                    timeQueued = timeNow,
                )
            }
        )
    }
}