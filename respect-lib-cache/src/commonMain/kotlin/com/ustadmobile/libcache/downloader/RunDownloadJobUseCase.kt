package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.entities.DownloadJobItem

interface RunDownloadJobUseCase {

    suspend operator fun invoke(downloadJobUid: Int)

    suspend operator fun invoke(
        items: List<DownloadJobItem>,
        onProgress: (DownloadProgressUpdate) -> Unit = { },
        onStatusUpdate: (DownloadStatusUpdate) -> Unit = { },
    )
}