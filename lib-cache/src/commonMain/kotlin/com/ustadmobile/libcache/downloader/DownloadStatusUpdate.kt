package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.entities.DownloadJobItem

data class DownloadStatusUpdate(
    val jobItem:  DownloadJobItem,
    val status: Int
)
