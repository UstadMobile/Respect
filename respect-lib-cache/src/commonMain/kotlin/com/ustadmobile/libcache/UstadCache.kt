package com.ustadmobile.libcache

import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

data class EntryLockRequest(
    val url: String,
    val remark: String = "",
    val publicationUid: Long = 0,
)

data class RemoveLockRequest(
    val url: String,
    val lockId: Long,
)

data class PublicationPinState(
    val status: Status,
    val totalSize: Long,
    val transferred: Long,
) {
    enum class Status(val flagVal: Int) {

        NOT_PINNED(0), PREPARING(2), IN_PROGRESS(3), READY(10), FAILED(20);


        companion object {

            const val NOT_PINNED_INT = 0

            const val PREPARING_INT = 2

            const val IN_PROGRESS_INT = 3
            const val READY_STATUS_INT = 10

            const val FAILED_INT = 20
        }
    }
}

/**
 *
 */
@Suppress("unused")
interface UstadCache {

    /**
     * CacheListener is not normally required, but can be needed in tests to wait for
     * caching a request to be completed.
     */
    interface CacheListener {

        fun onEntriesStored(storeRequest: List<CacheEntryToStore>)

    }


    /**
     * Filter that will be used by the cache to determine if a given entry should be stored using
     * compression. This should generally be true for text types e.g. css, javascript, html, json, etc.
     * Should not be used for types that are already compressed e.g. images, audio, video, zips, etc.
     *
     * When it is determined that an entry should be compressed, then it will be stored on disk as a
     * compressed file. When it is served, the content-encoding header will be used (which is stored
     * together with all other headers when added to the cache).
     */
    val storageCompressionFilter: CacheStorageCompressionFilter

    /**
     * Store a set of requests with their corresponding response.
     */
    suspend fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener? = null,
    ): List<StoreResult>

    /**
     * Update the last validated information for a given set of urls. This should be performed when
     * another component (e.g. the OkHttp interceptor) has performed a successful validation e.g.
     * received a Not-Modified response from the origin server.
     *
     * The not-modified response from the origin server will likely not have all the original
     * headers (e.g. content-length, content-type, etc). This is valid. The not-modified
     * response can likely contain validation / cache related headers like Age, Last-Modified,
     * etc. Generally, any headers from the validation response will override the previous
     * headers.
     *
     * An exception is content-length: some servers e.g. KTOR (incorrectly) specify a
     * content-length of zero on 304 responses. This is not valid. The content-length
     * header will be filtered out. By definition: 304 means NOT MODIFIED, and if it was
     * not modified, the content-length should NOT have changed.
     *
     * The headers stored in the cache will be updated from the validated entry, with any invalid
     * headers (as outlined above) filtered out.
     */
    suspend fun updateLastValidated(validatedEntry: ValidatedEntry)

    /**
     * Retrieve a response for the given entry.
     *
     * @param request HttpRequest
     * @return HttpResponse if the entry is in the cache, null otherwise
     */
    suspend fun retrieve(
        request: IHttpRequest,
    ): IHttpResponse?

    suspend fun getCacheEntry(url: String): CacheEntry?

    /**
     * Get a list of the locks that exist for a given entry
     */
    suspend fun getLocks(url: String): List<RetentionLock>

    /**
     * Run a bulk query to check if the given urls are available in the cache.
     *
     * @param urls a set of URLs to check to see if they are available in the cache
     * @return A map of the which URLs are cached (url to boolean)
     */
    suspend fun getEntries(
        urls: Set<String>
    ): Map<String, CacheEntry?>

    /**
     * Run a bulk query to see if the given urls are available from neighbor caches.
     */
    suspend fun getEntriesLocallyAvailable(
        urls: Set<String>
    ): Map<String, Boolean>


    /**
     * Create retention locks for the given urls. Retention locks are used to prevent a given url
     * from being evicted from the cache. When a user has selected an particular item as something
     * that they want to have available offline, it should not be removed until they decide otherwise,
     * even if it would normally be evicted due to not being recently accessed.
     *
     * Entries that have a retention lock will be stored in the PersistentPath (see CachePaths) to
     * ensure the OS does not delete them.
     */
    suspend fun addRetentionLocks(
        locks: List<EntryLockRequest>
    ): List<Pair<EntryLockRequest, RetentionLock>>

    /**
     * Remove the given retention locks. If all locks are removed, then the entry becomes eligible
     * for eviction (it is not immediately removed).
     *
     * When an entry has no remaining locks, then it will be moved into the cachePath (see CachePaths)
     * to allow the OS to delete it if desired.
     */
    suspend fun removeRetentionLocks(locksToRemove: List<RemoveLockRequest>)

    suspend fun findLocksByPublicationUid(publicationUid: Long): List<RetentionLock>



    /**
     * To pin a given publication
     * a) Create a PinnedPublication Entity
     * b) Create DownloadJobItem entities and RetentionLocks for each resource in the publication
     * c) Run the DownloadJob
     *
     * Use case flow:
     * a) EnqueuePinPublicationPrepareUseCase: create job using WorkManager/Quartz
     * b) PinPublicationPrepareUseCase: get publication manifest, create DownloadJobItem entities and
     *    add RetentionLocks
     * c) EnqueueDownloadJobUseCase: create job using WorkManager/Quartz
     * d) RunDownloadJobUseCase: download all required urls as per DownloadJobItem entities
     *
     */
    suspend fun pinPublication(manifestUrl: Url)

    /**
     * The state of the publication is:
     *
     * Pinned/Ready: there is a downloadjob for the given manifest url AND the job status is completed
     *
     * Pinned/in-progress: There is a downloadjob for the given manifest URL nad the job status is
     * in progress
     *
     * Pinned/failed: most recent downloadjob for given manifest url failed.
     *
     * Otherwise: not pinned
     */
    fun publicationPinState(manifestUrl: Url): Flow<PublicationPinState>

    /**
     * Update the status of the most recent download job for the given manifest URL to unpinned.
     * Remove any retention locks
     *
     */
    suspend fun unpinPublication(manifestUrl: Url)

    fun close()

    companion object {

        const val HEADER_FIRST_STORED_TIMESTAMP = "UCache-First-Stored"

        const val HEADER_LAST_VALIDATED_TIMESTAMP = "UCache-Last-Validated"

        const val DEFAULT_SIZE_LIMIT = (100 * 1024 * 1024).toLong()

    }


}