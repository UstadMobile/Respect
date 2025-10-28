package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.entities.DownloadJob
import io.ktor.http.Url

/**
 * Enqueue downloading a publication resources given the Web Publication's manifest URL.
 */
interface EnqueuePinPublicationPrepareUseCase {

    suspend operator fun invoke(manifestUrl: Url): DownloadJob

}