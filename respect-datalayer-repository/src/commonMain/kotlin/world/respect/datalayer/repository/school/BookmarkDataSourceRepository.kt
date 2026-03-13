package world.respect.datalayer.repository.school

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemoteIfNotNull
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.BookmarkDataSourceLocal
import world.respect.datalayer.school.model.Bookmark
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.DataLayerTags
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis


class BookmarkDataSourceRepository(


    override val local: BookmarkDataSourceLocal,
    override val remote: BookmarkDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : BookmarkDataSource, RepositoryModelDataSource<Bookmark> {
    override fun getBookmarkStatus(
        personUid: String,
        url: Url
    ): Flow<Boolean> {
        return local.getBookmarkStatus(personUid, url)
    }

    override suspend fun store(list: List<Bookmark>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.BOOKMARK,
                    uid = it.personUid,
                    timeQueued = timeNow,
                )
            }
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: BookmarkDataSource.GetListParams
    ): DataLoadState<List<Bookmark>> {
        val remote = try {
            remote.list(loadParams, listParams).also {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        } catch (e: Throwable) {
            Napier.w(
                message = "BookmarkDataSourceRepository.list() failed:",
                throwable = e,
                tag = DataLayerTags.TAG_DATALAYER
            )
            null
        }

        return local.list(loadParams, listParams).combineWithRemoteIfNotNull(remote)
    }

    override suspend fun findBookmarksWithMissingPublication(personUid: String): List<Bookmark> {
        return local.findBookmarksWithMissingPublication(personUid)
    }

}
