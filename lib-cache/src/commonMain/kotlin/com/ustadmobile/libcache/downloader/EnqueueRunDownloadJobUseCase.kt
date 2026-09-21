package com.ustadmobile.libcache.downloader

interface EnqueueRunDownloadJobUseCase {

    operator fun invoke(downloadJobUid: Int)

}