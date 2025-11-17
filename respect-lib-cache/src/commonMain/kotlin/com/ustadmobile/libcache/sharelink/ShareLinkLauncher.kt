package com.ustadmobile.libcache.sharelink

interface ShareLinkLauncher {
    suspend fun launch(link: String)

    companion object {
        const val MIME_TYPE = "text/plain"
    }

}
