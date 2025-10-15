package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.EntryLockRequest
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.DownloadJobItem
import com.ustadmobile.libcache.db.entities.PinnedPublication
import com.ustadmobile.libcache.util.withWriterTransaction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.http.contentLength
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.findLearningUnitAcquisitionLinks
import world.respect.libutil.ext.resolve
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Fetches the publication manifest (over http) and goes through all of the resource links.
 * For each resource:
 *
 *  a) Create a DownloadJobItem
 *  b) Add a RetentionLock
 *  c) If the resource does not have a size provided in the ReadiumLink, then make an HTTP HEAD request
 *     to get the size and update the TransferJobItem accordingly.
 *  d) Enqueue RunDownloadJob to run the actual download.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PinPublicationPrepareUseCase(
    private val httpClient: HttpClient,
    private val db: UstadCacheDb,
    private val cache: UstadCache,
    private val enqueueRunDownloadJobUseCase: EnqueueRunDownloadJobUseCase,
) {

    /**
     *
     */
    suspend operator fun invoke(
        downloadJobUid: Int
    ) {
        val downloadJob = db.downloadJobDao.findByUid(downloadJobUid)
            ?: throw IllegalArgumentException("No transfer job with uid $downloadJobUid")
        val manifestJobItem = db.downloadJobItemDao.findPendingByJobUid(downloadJobUid).first()

        val manifestUrl = downloadJob.djPubManifestUrl
            ?: throw IllegalArgumentException("no manifest url")

        val publication: OpdsPublication = httpClient.get(manifestUrl).body()

        val resourceAndAcquireJobItems = buildList {
            val linksToDownload = (publication.resources ?: emptyList()) +
                    publication.findLearningUnitAcquisitionLinks()

            addAll(
                linksToDownload.map { resource ->
                    DownloadJobItem(
                        djiDjUid = downloadJobUid,
                        djiUrl = manifestUrl.resolve(resource.href),
                        djiTotalSize = (resource.size ?: 0).toLong()
                    )
                }
            )

            add(manifestJobItem)
        }.distinctBy { item -> item.djiUrl }

        val downloadJobItemWithSize: MutableList<DownloadJobItem> = CopyOnWriteArrayList()

        coroutineScope {
            val jobItemProducer = produce(
                capacity = Channel.UNLIMITED
            ) {
                resourceAndAcquireJobItems.forEach { send(it) }
                close()
            }

            val jobs = (1.. PARALLEL_SIZE_FETCH_LIMIT).map {
                async {
                    for (item in jobItemProducer) {
                        val jobItemWithSize = if(item.djiTotalSize <= 0) {
                            val contentLength = httpClient.head(item.djiUrl) {
                                header("cache-control", "no-cache, no-store")
                            }.contentLength()

                            item.copy(
                                djiTotalSize = contentLength ?: 0
                            )
                        }else {
                            item
                        }
                        downloadJobItemWithSize.add(jobItemWithSize)
                    }
                }
            }

            jobs.awaitAll()
        }

        cache.addRetentionLocks(
            downloadJobItemWithSize.map {
                EntryLockRequest(
                    url = it.djiUrl.toString(),
                    publicationUid = downloadJob.djPubManifestHash,
                )
            }
        )

        db.withWriterTransaction {
            db.downloadJobItemDao.upsertList(downloadJobItemWithSize)
            db.pinnedPublicationDao.insert(
                PinnedPublication(
                    ppUrlHash = downloadJob.djPubManifestHash,
                    title = downloadJob.djName ?: ""
                )
            )
        }

        enqueueRunDownloadJobUseCase(downloadJobUid)
    }


    companion object {

        const val PARALLEL_SIZE_FETCH_LIMIT = 4

        const val DEFAULT_MAX_ATTEMPTS = 5

    }
}