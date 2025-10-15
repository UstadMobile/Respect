package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.DownloadJobItem
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import com.ustadmobile.libcache.okhttp.await
import com.ustadmobile.libcache.util.withWriterTransaction
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class RunDownloadJobUseCaseImpl(
    private val okHttpClient: OkHttpClient,
    private val db: UstadCacheDb,
    private val httpCache: UstadCache,
): RunDownloadJobUseCase {

    private suspend fun downloadItemsFromChannelProcessor(
        channel: ReceiveChannel<DownloadJobItem>,
        onProgress: (DownloadProgressUpdate) -> Unit,
        onStatusUpdate: (DownloadStatusUpdate) -> Unit
    ) = coroutineScope {
        val buffer = ByteArray(8192)
        async {
            for(queueItem in channel) {
                val logPrefix = "RunDownloadJobUseCaseImpl: " +
                        "#${queueItem.djiUid} " +
                        "${queueItem.djiUrl} "

                //Pull the item through OkHttp. This will pull it through the lib-cache interceptor.
                Napier.v { "$logPrefix : channel: start"}
                try {
                    onStatusUpdate(
                        DownloadStatusUpdate(
                            jobItem = queueItem,
                            status = TransferJobItemStatus.STATUS_IN_PROGRESS_INT,
                        )
                    )

                    val request = Request.Builder()
                        .url(queueItem.djiUrl.toString())
                        .apply {
                            queueItem.djiPartialTmpFile?.also {
                                header("X-Interceptor-Partial-File", it)
                            }
                        }
                        .build()

                    val response = okHttpClient.newCall(request).await()

                    var totalBytesRead = 0L
                    var bytesRead = 0

                    response.body.byteStream().use { inStream ->
                        while(isActive && inStream.read(buffer).also { bytesRead = it } != -1) {
                            totalBytesRead += bytesRead
                            onProgress(
                                DownloadProgressUpdate(
                                    jobItem = queueItem,
                                    bytesTransferred = totalBytesRead
                                )
                            )
                        }
                    }

                    Napier.v { "$logPrefix channel: completed"}
                    onStatusUpdate(
                        DownloadStatusUpdate(
                            jobItem = queueItem,
                            status = TransferJobItemStatus.STATUS_COMPLETE_INT,
                        )
                    )
                }catch(e: Throwable) {
                    Napier.i("$logPrefix : channel: Exception downloading", e)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(
        items: List<DownloadJobItem>,
        onProgress: (DownloadProgressUpdate) -> Unit,
        onStatusUpdate: (DownloadStatusUpdate) -> Unit
    ) {
        //Prime the cache to get the status of all entries that are going to be required. This will
        // avoid (potentially hundreds) of separate SQLite queries for each new request by triggering
        // the cache to load entry status into the LRU in-memory map
        httpCache.getEntries(items.map { it.djiUrl.toString() }.toSet())

        coroutineScope {
            val receiveChannel = produce(
                capacity = Channel.UNLIMITED
            ) {
                items.forEach { send(it) }
                close()
            }

            val jobs = (0..4).map {
                downloadItemsFromChannelProcessor(
                    channel = receiveChannel,
                    onProgress = onProgress,
                    onStatusUpdate = onStatusUpdate,
                )
            }

            jobs.awaitAll()
        }
    }



    override suspend fun invoke(downloadJobUid: Int) {
        val logPrefix = "BlobDownloadClientUseCaseCommonJvm (#$downloadJobUid)"
        val transferJob = db.downloadJobDao.findByUid(downloadJobUid)
            ?: throw IllegalArgumentException("$logPrefix: TransferJob #$downloadJobUid does not exist")
        val transferJobItems = db.downloadJobItemDao.findPendingByJobUid(
            downloadJobUid)

        coroutineScope {
            val transferJobItemStatusUpdater = DownloadJobItemStatusUpdater(db,  this)
            try {
                invoke(
                    items = transferJobItems,
                    onProgress = transferJobItemStatusUpdater::onProgressUpdate,
                    onStatusUpdate = transferJobItemStatusUpdater::onStatusUpdate,
                )

                val numIncompleteItems = db.withWriterTransaction {
                    transferJobItemStatusUpdater.onFinished()
                    transferJobItemStatusUpdater.commit(downloadJobUid)
                    db.downloadJobItemDao.findNumberJobItemsNotComplete(downloadJobUid)
                }

                if(numIncompleteItems != 0) {
                    throw IllegalStateException("$logPrefix: not complete.")
                }
                Napier.d { "$logPrefix complete!"}
            }catch(e: Throwable) {
                Napier.e("$logPrefix Exception. Attempt has failed", e)
                withContext(NonCancellable) {
                    transferJobItemStatusUpdater.onFinished()
                }

                throw e
            }
        }

    }

}