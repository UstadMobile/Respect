package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.entities.DownloadJobItem

data class DownloadProgressUpdate(
    val jobItem:  DownloadJobItem,
    val bytesTransferred: Long
) {

    override fun toString(): String {
        return "Uid #${jobItem.djiUid} transferred=${bytesTransferred} bytes"
    }

}
