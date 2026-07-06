package com.ustadmobile.libcache

import androidx.room.Transactor.SQLiteTransactionType
import androidx.room.deferredTransaction
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.MergedHeaders
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import com.ustadmobile.ihttp.headers.asString
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.headers.mapHeaders
import com.ustadmobile.ihttp.iostreams.NullOutputStream
import com.ustadmobile.libcache.cachecontrol.ResponseValidityChecker
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.CacheEntryAndMetadata
import com.ustadmobile.libcache.db.entities.RetentionLock
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.moveWithFallback
import com.ustadmobile.libcache.io.requireMetadata
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.libcache.io.transferToAndGetSha256
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.logging.UstadCacheLogger
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.libcache.response.CacheResponse
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.cachecontrol.CacheControlFreshnessChecker
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import com.ustadmobile.libcache.downloader.EnqueuePinPublicationPrepareUseCase
import com.ustadmobile.libcache.headers.hasCacheValidators
import com.ustadmobile.libcache.headers.integrity
import com.ustadmobile.libcache.novarysearch.NoVarySearch
import com.ustadmobile.libcache.novarysearch.normalizeForNoVarySearch
import com.ustadmobile.libcache.novarysearch.removeAllParams
import com.ustadmobile.libcache.response.ByteArrayResponse
import com.ustadmobile.libcache.util.concurrentSafeMapOf
import io.github.reactivecircus.cache4k.Cache
import io.github.reactivecircus.cache4k.CacheEvent
import io.ktor.http.Url
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.libxxhash.XXStringHasher
import kotlin.also
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 *
 * @param sizeLimit A function that returns the current size limit for the cache. This will be
 *        invoked on the periodic trims that are run. The limit applies to evictable entries e.g.
 *        entries which do not have any retentionlock.
 * @param databaseCommitInterval the interval period to commit updates to the database. When entries
 */
@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class UstadCacheImpl(
    private val fileSystem: FileSystem = SystemFileSystem,
    cacheName: String = "",
    private val pathsProvider: CachePathsProvider,
    private val db: UstadCacheDb,
    sizeLimit: () -> Long = { UstadCache.DEFAULT_SIZE_LIMIT },
    private val logger: UstadCacheLogger? = null,
    private val listener: UstadCache.CacheListener? = null,
    private val databaseCommitInterval: Int = 2_000,
    private val trimInterval: Int = 30_000,
    private val responseValidityChecker: ResponseValidityChecker = ResponseValidityChecker(),
    private val trimmer: UstadCacheTrimmer = UstadCacheTrimmer(
        db = db,
        fileSystem = fileSystem,
        logger = logger,
        sizeLimit = sizeLimit,
    ),
    override val storageCompressionFilter: CacheStorageCompressionFilter = DefaultCacheCompressionFilter(),
    private val xxStringHasher: XXStringHasher,
    private val enqueuePinPublicationPrepareUseCase: EnqueuePinPublicationPrepareUseCase,
    private val freshnessChecker: CacheControlFreshnessChecker,
    private val extraHeaderProvider: UstadCacheExtraHeaderProvider? = null,
) : UstadCache {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val tmpCounter = atomic(0)

    private val lockIdAtomic = atomic(Clock.System.now().toEpochMilliseconds())

    private val logPrefix = "UstadCache($cacheName):"

    private val pendingLastAccessedUpdates = atomic(emptyList<LastAccessedUpdate>())

    /**
     * Memory cache of URL without any search parameters to a list of URLs keys
     *
     * e.g. Where NoVarySearch covers all search parameters, key and value in list will be the same
     * http://localhost/user -> [http://localhost/user]
     *
     * Where only some search parameters are covered e.g. No-Vary-Search: params("id")
     * http://localhost/user -> [http://localhost/user?lang=en]
     *
     * This allows the retrieve function to find potential matching cache entries even when the URL
     * itself is not an exact match.
     */
    //private val urlsWithoutSearchToFullUrlsCache = Cache.Builder<Url, Set<Url>>().build()

    /**
     * Data class that is used to track the status of a CacheEntryToStore as it is processed.
     *
     * @param cacheEntry the CacheEntry entity as it will be saved into the database
     * @param entryToStore the entryToStore request that provided as an argument to the store function
     * @param tmpFile the temporary file where data is being kept
     * @param responseHeaders the response headers (canonical) as they will be stored including the
     *        etag integrity values etc. that get added by default
     * @param tmpFileNeedsDeleted if true, then the tmpFile must be deleted before completing
     *        the store function. Can be false when responseBodyTmpLocalPath is provided, because
     *        then the responseBodyTmpLocalPath will be moved (not copied).
     * @param previousStorageUriToDelete if it is determined that new data will be replacing old
     *        data, then the previous body data will be deleted.
     *
     */
    private data class CacheEntryInProgress(
        val cacheEntry: CacheEntry,
        val entryToStore: CacheEntryToStore,
        val tmpFile: Path,
        val responseHeaders: IHttpHeaders,
        val tmpFileNeedsDeleted: Boolean = false,
        val lockId: Long = 0,
        val previousStorageUriToDelete: String? = null,
    )

    data class LastAccessedUpdate(
        val key: String,
        val accessTime: Long,
    )


    private val pendingCacheUpdates = concurrentSafeMapOf<String, CacheEvent.Updated<String, CacheEntryAndMetadata>>()

    private val memoryCache = Cache.Builder<String, CacheEntryAndMetadata>()
        .eventListener { event ->
            when(event) {
                is CacheEvent.Updated -> {
                    pendingCacheUpdates.compute(event.key) { _, prev ->
                        if(prev == null) {
                            event
                        }else {
                            CacheEvent.Updated(event.key, prev.oldValue, event.newValue)
                        }
                    }
                }

                else -> {
                    //do nothing
                }
            }
        }
        .build()

    suspend fun Cache<String, CacheEntryAndMetadata>.getOrLoadFromDb(
        key: String,
    ): CacheEntryAndMetadata {
        return get(key) {
            CacheEntryAndMetadata(
                urlKey = key,
                entry = db.cacheEntryDao.findEntryByKey(key),
                locks = db.retentionLockDao.findByKey(key),
            )
        }
    }

    suspend fun Cache<String, CacheEntryAndMetadata>.update(
        key: String,
        update: (CacheEntryAndMetadata) -> CacheEntryAndMetadata,
    ) {
        val current = getOrLoadFromDb(key)
        current.mutex.withLock {
            val newVal = update(current)
            put(key, newVal)
        }
    }

    init {
        scope.launch {
            while(isActive) {
                delay(databaseCommitInterval.milliseconds)
                commit()
            }
        }

        scope.launch {
            while(isActive) {
                delay(trimInterval.milliseconds)
                commit()
                trimmer.trim()
            }
        }

        scope.launch {
            trimmer.evictedEntriesFlow.collect { evictedEntries ->
                evictedEntries.forEach { evictedKey ->
                    memoryCache.invalidate(evictedKey)
                }
            }
        }
    }

    override suspend fun store(
        storeRequest: List<CacheEntryToStore>,
        progressListener: StoreProgressListener?
    ): List<StoreResult> {
        val md5Digest = Md5Digest()
        val timeNow = Clock.System.now().toEpochMilliseconds()
        val entryPaths = pathsProvider()

        try {
            logger?.d(LOG_TAG) { "$logPrefix store request for ${storeRequest.size} entries" }

            /**
             * Go through everything that is requested to be stored: create a list of CacheEntryInProgress
             * which contains the CacheEntry entity (headers, request status, etc) and a tmp
             * file containing the request body. Run checksums as needed to get a SHA256 checksum
             * for every response body.
             */
            val entriesWithTmpFileAndIntegrityInfo = storeRequest.map { entryToStore ->
                val response = entryToStore.response

                fileSystem.createDirectories(entryPaths.tmpWorkPath)
                val tmpFile = Path(entryPaths.tmpWorkPath,
                    "${tmpCounter.incrementAndGet()}.tmp")
                val url = entryToStore.request.url
                val storeCompressionType = storageCompressionFilter(
                    url = entryToStore.request.url,
                    requestHeaders = entryToStore.request.headers,
                    responseHeaders = entryToStore.response.headers
                )
                val responseCompression = CompressionType.byHeaderVal(
                    entryToStore.response.headers["content-encoding"]
                )
                val overrideHeaders = mutableMapOf<String, List<String>>()

                @Suppress("ArrayInDataClass")
                data class Sha256AndInflatedSize(val sha256: ByteArray?, val inflatedSize: Long)

                val (sha256IntegrityFromTransfer, uncompressedSize) = if(
                    entryToStore.responseBodyTmpLocalPath != null && storeCompressionType == responseCompression
                ) {
                    //If the entry to store is in a temporary path where it is acceptable to just
                    //move the file into the cache, and it is already compressed with the desired
                    // compression type for storage, then we will move (instead of copying) the file
                    fileSystem.moveWithFallback(entryToStore.responseBodyTmpLocalPath, tmpFile)
                    val inflatedSize = if(storeCompressionType == CompressionType.NONE) {
                        fileSystem.requireMetadata(tmpFile).size
                    }else {
                        //"transfer" to a nulloutputstream sink to count uncompressed size
                        fileSystem.source(tmpFile).buffered()
                            .uncompress(storeCompressionType).transferTo(NullOutputStream().asSink())
                    }

                    Sha256AndInflatedSize(null, inflatedSize)
                }else {
                    val bodySource = response.bodyAsSource()

                    if(bodySource == null) {
                        val e = IllegalArgumentException("Response for $url has " +
                                "no body. That should not have been stored in cache. Something badly wrong.")
                        logger?.e(LOG_TAG, "$logPrefix BodySource for ${entryToStore.request.url} is null", e)
                        throw e
                    }

                    val transferResult = bodySource.transferToAndGetSha256(tmpFile,
                        responseCompression, storeCompressionType)
                    overrideHeaders["content-encoding"] = listOf(storeCompressionType.headerVal)
                    overrideHeaders["content-length"] = listOf(fileSystem.requireMetadata(tmpFile).size.toString())

                    Sha256AndInflatedSize(transferResult.sha256, transferResult.transferred)
                }

                val integrityFromHeaders = if(entryToStore.skipChecksumIfProvided)
                    response.headers.integrity()
                else
                    null

                val integrity = sha256IntegrityFromTransfer?.let { sha256Integrity(it) }
                    ?: integrityFromHeaders
                    ?: sha256Integrity(fileSystem.source(tmpFile).buffered().useAndReadSha256())

                val effectiveHeaders = if(overrideHeaders.isNotEmpty()) {
                    MergedHeaders(IHttpHeaders.fromMap(overrideHeaders), response.headers)
                }else {
                    response.headers
                }

                logger?.v(LOG_TAG, "$logPrefix copied request data for $url to $tmpFile (integrity=$integrity)")

                val noVarySearchHeader = effectiveHeaders["No-Vary-Search"]
                val urlForKey = if (noVarySearchHeader != null) {
                    Url(entryToStore.request.url).normalizeForNoVarySearch(
                        NoVarySearch.parse(noVarySearchHeader)
                    ).toString()
                }else {
                    entryToStore.request.url
                }

                val urlWithoutParams = if(noVarySearchHeader != null) {
                    Url(url).removeAllParams().toString()
                }else {
                    null
                }

                CacheEntryInProgress(
                    cacheEntry = CacheEntry(
                        key = md5Digest.urlKey(urlForKey),
                        url = urlForKey,
                        urlWithoutSearch = urlWithoutParams,
                        keyWithoutSearch = urlWithoutParams?.let { md5Digest.urlKey(it) },
                        integrity = integrity,
                        statusCode = entryToStore.response.responseCode,
                        responseHeaders = effectiveHeaders.asString(),
                        lastValidated = timeNow,
                        lastAccessed = timeNow,
                        uncompressedSize = uncompressedSize
                    ),
                    entryToStore = entryToStore,
                    tmpFile = tmpFile,
                    responseHeaders = entryToStore.response.headers,
                )
            }

            val processedEntries = entriesWithTmpFileAndIntegrityInfo.map { entryInProgress ->
                val key = entryInProgress.cacheEntry.key
                val entryInCache = memoryCache.getOrLoadFromDb(key)

                val storedEntry = entryInCache.entry

                val storedEntryHeaders = storedEntry?.responseHeaders?.let {
                    IHttpHeaders.fromString(it)
                }

                val etagOrLastModifiedMatches = if(storedEntryHeaders != null) {
                    responseValidityChecker.isMatchingEtagOrLastModified(
                        storedEntryHeaders, entryInProgress.entryToStore.response.headers
                    )
                }else {
                    false
                }

                if(storedEntry != null && etagOrLastModifiedMatches && storedEntryHeaders != null) {
                    /* If the entry is already saved and still valid. We will not store the body,
                     * but we will update the CacheEntry so that the last validated and last accessed
                     * times are updated.
                     *
                     * Because the body data will not be modified, the content-length and
                     * content-encoding MUST NOT be changed.
                     */
                    val overrideHeaders = buildMap {
                        NOT_MODIFIED_IGNORE_HEADERS.forEach { headerName ->
                            storedEntryHeaders[headerName]?.also { storedEntryHeaderVal ->
                                put(headerName, listOf(storedEntryHeaderVal))
                            }
                        }
                    }

                    entryInProgress.copy(
                        cacheEntry = entryInProgress.cacheEntry.copy(
                            storageUri = storedEntry.storageUri,
                            storageSize = storedEntry.storageSize,
                            responseHeaders = MergedHeaders(
                                IHttpHeaders.fromMap(overrideHeaders),
                                entryInProgress.responseHeaders,
                                storedEntryHeaders,
                            ).asString()
                        )
                    ).also {
                        memoryCache.put(
                            key = key,
                            value = entryInCache.copy(entry = it.cacheEntry)
                        )

                        fileSystem.delete(entryInProgress.tmpFile, mustExist = false)
                    }
                }else {
                    //Entry does not validate,
                    //The new entry does not validate, so we will need to store the new body.
                    val destPaths = pathsProvider()
                    val destPathParent = if(
                        entryInCache.locks.isNotEmpty() ||
                        entryInProgress.entryToStore.createRetentionLock
                    ) {
                        destPaths.persistentPath
                    }else {
                        destPaths.cachePath
                    }
                    fileSystem.createDirectories(destPathParent)
                    val destPath = Path(
                        base = destPathParent.toString(),
                        Uuid.random().toString()
                    )
                    fileSystem.moveWithFallback(entryInProgress.tmpFile, destPath)

                    entryInProgress.copy(
                        cacheEntry = entryInProgress.cacheEntry.copy(
                            storageUri = destPath.toString(),
                            storageSize = fileSystem.metadataOrNull(destPath)?.size ?: 0,
                        )
                    ).also {
                        memoryCache.put(
                            key = key,
                            value = entryInCache.copy(entry = it.cacheEntry)
                        )
                        fileSystem.delete(entryInProgress.tmpFile, mustExist = false)
                    }
                }
            }

            logger?.v(LOG_TAG) {
                "$logPrefix cacheEntries created ${entriesWithTmpFileAndIntegrityInfo.size} entries"
            }

            listener?.onEntriesStored(storeRequest)
            return processedEntries.map {
                StoreResult(
                    urlKey = it.cacheEntry.key,
                    request = it.entryToStore.request,
                    response = it.entryToStore.response,
                    integrity = it.cacheEntry.integrity!!,
                    storageSize = it.cacheEntry.storageSize,
                    lockId = it.lockId
                )
            }
        }catch(e: Throwable) {
            throw IllegalStateException("Could not cache", e)
        }
    }

    /**
     * Retrieve a response from the cache, if available. The response might be stale.
     *
     * NOTE: If we know in advance that a particular batch is going to be requested, we can run
     * a statusCheckCache to avoid running 100s-1000+ SQL queries for tiny jsons etc.
     *
     */
    override suspend fun retrieve(request: IHttpRequest): IHttpResponse? {
        logger?.i(LOG_TAG, "$logPrefix Retrieve ${request.url}")

        val urlKey = Md5Digest().urlKey(request.url)
        val entryAndLocks = memoryCache.getOrLoadFromDb(urlKey)

        val entry = entryAndLocks.entry
        if(entry != null) {
            if(fileSystem.exists(Path(entry.storageUri))) {
                logger?.d(LOG_TAG, "$logPrefix FOUND ${request.url}")
                pendingLastAccessedUpdates.update { prev ->
                    prev + LastAccessedUpdate(entryAndLocks.urlKey, Clock.System.now().toEpochMilliseconds())
                }

                val responseHeaders = iHeadersBuilder {
                    takeFrom(IHttpHeaders.fromString(entry.responseHeaders))
                    extraHeaderProvider?.invoke(request)?.also {
                        takeFrom(it.asIHttpHeaders())
                    }
                    header(HEADER_LAST_VALIDATED_TIMESTAMP, entry.lastValidated.toString())
                }

                /*
                 * If the request received had its own explicitly set validation info AND the cache
                 * is already fresh THEN we can reply an empty 304 not modified response.
                 */
                val requestHasValidators = request.headers.hasCacheValidators()
                val reply304 = requestHasValidators && freshnessChecker(
                    requestHeaders = request.headers,
                    responseHeaders = responseHeaders,
                    responseLastValidated = entry.lastValidated,
                    responseFirstStoredTime = systemTimeInMillis() //TODO: Set this properly
                ).isFresh

                return if(reply304) {
                    ByteArrayResponse(
                        request = request,
                        mimeType = responseHeaders["content-type"] ?: "application/octet-stream",
                        responseCode = 304,
                        body = ByteArray(0),
                    )
                }else {
                    CacheResponse(
                        fileSystem = fileSystem,
                        request = request,
                        headers = responseHeaders,
                        storageUri = entry.storageUri,
                        httpResponseCode = entry.statusCode,
                        uncompressedSize = entry.uncompressedSize,
                    )
                }
            }else {
                logger?.d(LOG_TAG, "$logPrefix Entry deleted externally:  ${request.url}")
//                if(entryAndLocks.locks.isEmpty()) {
//                    logger?.d(LOG_TAG, "$logPrefix Entry deleted externally: " +
//                            "${request.url} - has no locks, so removing from cache")
//
//                    lruMap.computeIfPresent(entryAndLocks.urlKey) { _, prev ->
//                        prev.copy(
//                            entry = null
//                        )
//                    }
//
//                    pendingCacheEntryUpserts.update { prev ->
//                        prev.filter { it.key != entryAndLocks.urlKey }
//                    }
//
//                    pendingCacheEntryDeletes.update { prev ->
//                        prev + entry
//                    }
//                }else {
//                    logger?.w(LOG_TAG, "$logPrefix Entry deleted externally: " +
//                            "${request.url} - BUT IT HAD LOCKS!!! Not good!")
//                }
            }
        }

        logger?.d(LOG_TAG, "$logPrefix MISS ${request.url}")
        return null
    }

    override suspend fun updateLastValidated(validatedEntry: ValidatedEntry) {
        val md5 = Md5Digest()
        val timeNow = Clock.System.now().toEpochMilliseconds()

        memoryCache.update(md5.urlKey(validatedEntry.url)) { prevEntry ->
            val existingEntry = prevEntry.entry
            if(existingEntry != null) {
                val existingHeaders = IHttpHeaders.fromString(existingEntry.responseHeaders)

                val newHeadersCorrected = validatedEntry.headers.mapHeaders { headerName, headerValue ->
                    when {
                        NOT_MODIFIED_IGNORE_HEADERS.any { headerName.equals(it, true) } -> null
                        else -> headerValue
                    }
                }
                val newHeaders = MergedHeaders(newHeadersCorrected, existingHeaders)

                prevEntry.copy(
                    entry = existingEntry.copy(
                        responseHeaders = newHeaders.asString(),
                        lastValidated = timeNow,
                        lastAccessed = timeNow,
                    )
                )
            }else {
                prevEntry
            }
        }
    }

    override suspend fun getCacheEntry(url: String): CacheEntry? {
        return memoryCache.getOrLoadFromDb(Md5Digest().urlKey(url)).entry?.copy()
    }

    override suspend fun getLocks(url: String): List<RetentionLock> {
        return memoryCache.getOrLoadFromDb(Md5Digest().urlKey(url)).locks
    }

    override suspend fun getEntries(urls: Set<String>): Map<String, CacheEntry> {
        val md5 = Md5Digest()
        return db.useReaderConnection { con ->
            con.deferredTransaction {
                urls.mapNotNull { url ->
                    memoryCache.getOrLoadFromDb(md5.urlKey(url)).entry?.let { entry ->
                        url to entry
                    }
                }.toMap()
            }
        }
    }

    override suspend fun getEntriesLocallyAvailable(urls: Set<String>): Map<String, Boolean> {
        val hashesToUrl = urls.associateBy {
            xxStringHasher.hash(it)
        }

        val availableEntryMap = mutableMapOf<String, Boolean>()


        urls.chunked(100).forEach { chunkedList ->
            val availableHashes = db.neighborCacheEntryDao.findAvailableEntries(
                chunkedList.map { xxStringHasher.hash(it) }
            )

            availableHashes.forEach { availableHash ->
                val availableUrl = hashesToUrl[availableHash]
                if(availableUrl != null) {
                    availableEntryMap[availableUrl] = true
                }else {
                    logger?.w(LOG_TAG, "Strangely could not find url in getEntriesAvailable")
                }
            }
        }


        return availableEntryMap
    }

    private fun CacheEntry.isStoredIn(parent: Path): Boolean {
        val currentPath = Path(storageUri)
        return currentPath.toString().startsWith(parent.toString())
    }

    /**
     * Used when an existing cache entry is locked or unlocked
     */
    private fun CacheEntry.moveToNewPath(destParent: Path): CacheEntry? {
        val currentPath = Path(storageUri)
        if(!fileSystem.exists(currentPath))
            return null //file with body no longer exists. Might have been deleted by OS.

        if(!fileSystem.exists(destParent)) {
            fileSystem.createDirectories(destParent)
        }

        return if(!currentPath.toString().startsWith(destParent.toString())) {
            val newDestPath = Path(destParent, currentPath.name)
            logger?.d(LOG_TAG, "$logPrefix moveToNewPath (${this.url}) $currentPath -> $newDestPath")
            fileSystem.moveWithFallback(currentPath, newDestPath)
            copy(storageUri = newDestPath.toString())
        }else {
            this
        }
    }

    override suspend fun addRetentionLocks(
        locks: List<EntryLockRequest>
    ): List<Pair<EntryLockRequest, RetentionLock>> {
        logger?.v(LOG_TAG) {
            "$logPrefix add retention locks for ${locks.joinToString { it.url } }"
        }
        val md5Digest = Md5Digest()

        return locks.map { lockRequest ->
            val key = md5Digest.urlKey(lockRequest.url)
            val newLock = RetentionLock(
                lockId = lockIdAtomic.incrementAndGet(),
                lockKey = key,
                lockRemark = lockRequest.remark,
                lockPublicationUid = lockRequest.publicationUid,
            )

            memoryCache.update(key) { prev ->
                val isNewlyLocked = prev.locks.isEmpty()

                prev.copy(
                    entry = if(isNewlyLocked) {
                        val persistentPath = pathsProvider().persistentPath
                        prev.entry?.takeIf {
                            !it.isStoredIn(persistentPath)
                        }?.moveToNewPath(persistentPath) ?: prev.entry
                    }else {
                        prev.entry
                    },
                    locks = prev.locks + newLock
                )
            }
            Pair(lockRequest, newLock)
        }
    }

    /**
     * Lock removal is done by adding it to the pending list. This isn't urgent. This avoids a large
     * number of database transactions running when lots of small files are being uploaded
     */
    override suspend fun removeRetentionLocks(locksToRemove: List<RemoveLockRequest>) {
        logger?.v(LOG_TAG) {
            "$logPrefix remove retention locks for ${locksToRemove.joinToString { "#${it.lockId}${it.url}" } }"
        }

        val md5 = Md5Digest()
        locksToRemove.forEach { removeRequest ->
            memoryCache.update(md5.urlKey(removeRequest.url)) { prev ->
                val newLockList = prev.locks.filter { it.lockId != removeRequest.lockId }
                val isNewlyUnlocked = prev.locks.isNotEmpty() && newLockList.isEmpty()

                prev.copy(
                    locks = prev.locks.filter { it.lockId != removeRequest.lockId },
                    entry = if(isNewlyUnlocked) {
                        prev.entry?.moveToNewPath(pathsProvider().cachePath)
                    }else {
                        prev.entry
                    }
                )
            }
        }
    }

    override suspend fun findLocksByPublicationUid(publicationUid: Long): List<RetentionLock> {
        return db.retentionLockDao.findByPublicationUid(publicationUid)
    }

    override suspend fun pinPublication(manifestUrl: Url) {
        enqueuePinPublicationPrepareUseCase(manifestUrl)
    }

    override suspend fun unpinPublication(manifestUrl: Url) {
        val locks = findLocksByPublicationUid(
            xxStringHasher.hash(manifestUrl.toString())
        )

        removeRetentionLocks(locks.map {
            RemoveLockRequest(
                url = it.lockKey,
                lockId = it.lockId
            )
        })

        db.downloadJobDao.updateStatusByManifestHash(
            manifestHash = xxStringHasher.hash(manifestUrl.toString()),
            status = TransferJobItemStatus.STATUS_CANCELLED
        )

        //Do nothing yet
    }

    override fun publicationPinState(manifestUrl: Url): Flow<PublicationPinState> {
        return db.downloadJobItemDao.publicationPinState(
            pubManifestHash = xxStringHasher.hash(manifestUrl.toString())
        )
    }

    suspend fun commit() {
        val cacheUpdateEvents = pendingCacheUpdates.entries.toList()
        cacheUpdateEvents.forEach {
            pendingCacheUpdates.remove(it.key)
        }

        val lastAccessUpdates = pendingLastAccessedUpdates.getAndUpdate {
            emptyList()
        }
        val updatesMap = mutableMapOf<String, Long>()

        lastAccessUpdates.forEach {
            updatesMap[it.key] = it.accessTime
        }

        db.useWriterConnection { con ->
            con.withTransaction(SQLiteTransactionType.IMMEDIATE) {
                db.cacheEntryDao.upsertList(
                    entry = cacheUpdateEvents.mapNotNull { it.value.newValue.entry }
                )

                val newLocks = cacheUpdateEvents.flatMap { event ->
                    event.value.newValue.locks.filter { newLock ->
                        !event.value.oldValue.locks.any { it.lockId == newLock.lockId }
                    }
                }

                val deletedLocks = cacheUpdateEvents.flatMap { event ->
                    event.value.oldValue.locks.filter { oldLock ->
                        !event.value.newValue.locks.any { it.lockId == oldLock.lockId }
                    }
                }
                if(newLocks.isNotEmpty())
                    db.retentionLockDao.upsertList(newLocks)

                if(deletedLocks.isNotEmpty())
                    db.retentionLockDao.delete(deletedLocks)

                updatesMap.forEach {
                    db.cacheEntryDao.updateLastAccessedTime(it.key, it.value)
                }
            }
        }
    }

    override fun close() {
        scope.cancel()
        runBlocking {
            commit()
        }
    }

    companion object {
        const val LOG_TAG = "UstadCache"

        /**
         * When an entry is validated, most headers will be updated with those found on the 304
         * not modified response e.g. Age, Cache-Control, Last-Modified etc.
         *
         * All values on the 304 not-modified SHOULD be the same as would otherwise be returned, however:
         *  1) KTOR, and other servers, use content-length: 0 (a 304 not-modified response has no
         *     body, so that response has a length of zero, but strictly speaking, this is wrong)
         *  2) content-encoding : this could be changed internally (e.g. by updating what mime types
         *     are or are not compressed). When a 304 response is received, the response body stored
         *     on disk is not changed, so the content-encoding must NEVER change.
         */
        private val NOT_MODIFIED_IGNORE_HEADERS = listOf("content-length", "content-encoding")

    }
}